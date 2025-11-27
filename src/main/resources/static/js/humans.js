(function () {
    document.addEventListener('DOMContentLoaded', () => {
        setupPagination();
        setupWebSocket();
        const history = setupImportHistory();
        setupImport(history);
        setupImportHelp();
        setupThemeToggle();
        setupUserMenu();
    });

    function setupPagination() {
        const pagination = document.querySelector('.pagination');
        if (!pagination) {
            return;
        }
        const page = parseInt(pagination.getAttribute('data-page'), 10) || 0;
        const size = parseInt(pagination.getAttribute('data-size'), 10) || 10;
        const totalPages = parseInt(document.getElementById('humans-table')?.getAttribute('data-total-pages') || '0', 10);
        pagination.querySelectorAll('.page-btn').forEach(button => {
            button.addEventListener('click', () => {
                const direction = button.getAttribute('data-direction');
                let nextPage = page;
                if (direction === 'next' && page < totalPages - 1) {
                    nextPage = page + 1;
                } else if (direction === 'prev' && page > 0) {
                    nextPage = page - 1;
                }
                const url = new URL(window.location.href);
                url.searchParams.set('page', nextPage);
                url.searchParams.set('size', size);
                window.location.href = url.toString();
            });
        });
    }

    function setupWebSocket() {
        const sumEl = document.getElementById('impact-sum');
        const countEl = document.getElementById('human-count');
        if (!sumEl || typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            return;
        }
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);
        stompClient.debug = () => {};
        stompClient.connect({}, () => {
            const requestSummary = () => stompClient.send('/app/humans/summary', {});
            stompClient.subscribe('/topic/humans', () => requestSummary());
            stompClient.subscribe('/topic/humans-summary', message => {
                try {
                    const payload = JSON.parse(message.body);
                    if (payload.totalImpactSpeed !== undefined) {
                        sumEl.textContent = payload.totalImpactSpeed;
                    }
                    if (payload.totalCount !== undefined && countEl) {
                        countEl.textContent = payload.totalCount;
                    }
                } catch (e) {
                    console.error('Ошибка обработки сообщения', e);
                }
            });
            requestSummary();
        }, error => console.error('WS error', error));
    }

    function setupImport(historyController) {
        const history = historyController || { reload: () => {} };
        const form = document.getElementById('import-form');
        if (!form) {
            return;
        }
        const successBox = document.getElementById('import-success');
        const errorBox = document.getElementById('import-error');
        const endpoint = form.getAttribute('data-endpoint') || '/api/humans/import';

        const showMessage = (element, message) => {
            if (!element) {
                return;
            }
            element.textContent = message;
            element.hidden = false;
        };

        const hideMessage = element => {
            if (!element) {
                return;
            }
            element.hidden = true;
            element.textContent = '';
        };

        form.addEventListener('submit', async event => {
            event.preventDefault();
            hideMessage(successBox);
            hideMessage(errorBox);
            const fileInput = form.querySelector('input[type="file"]');
            if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                showMessage(errorBox, 'Выберите JSON-файл для импорта');
                return;
            }

            const formData = new FormData();
            formData.append('file', fileInput.files[0]);

            try {
                const response = await fetch(endpoint, {
                    method: 'POST',
                    body: formData
                });
                let payload = {};
                try {
                    payload = await response.json();
                } catch (parseError) {
                    // ignore parsing error for non-JSON responses
                }
                if (!response.ok) {
                    const message = payload.error || payload.message || 'Не удалось импортировать данные';
                    showMessage(errorBox, message);
                    return;
                }
                const message = payload.message || 'Импорт успешно завершён';
                showMessage(successBox, message);
                fileInput.value = '';
                history.reload();
            } catch (error) {
                console.error('Ошибка импорта', error);
                showMessage(errorBox, 'Произошла ошибка при отправке запроса');
            }
        });
    }

    function setupImportHistory() {
        const historyContainer = document.getElementById('import-history');
        if (!historyContainer) {
            return { reload: () => {} };
        }
        const endpoint = historyContainer.getAttribute('data-endpoint');
        const tbody = historyContainer.querySelector('tbody');
        const reloadButton = historyContainer.querySelector('[data-history-reload]');
        const isAdmin = historyContainer.getAttribute('data-admin') === 'true';

        const render = (items) => {
            if (!tbody) {
                return;
            }
            tbody.innerHTML = '';
            if (!items || items.length === 0) {
                const emptyRow = document.createElement('tr');
                const cell = document.createElement('td');
                cell.textContent = 'Операций пока нет';
                cell.colSpan = isAdmin ? 6 : 5;
                cell.classList.add('empty');
                emptyRow.appendChild(cell);
                tbody.appendChild(emptyRow);
                return;
            }
            items.forEach(item => {
                const row = document.createElement('tr');

                const idCell = document.createElement('td');
                idCell.textContent = item.id;
                row.appendChild(idCell);

                if (isAdmin) {
                    const initiatorCell = document.createElement('td');
                    initiatorCell.textContent = item.initiator || '—';
                    row.appendChild(initiatorCell);
                }

                const statusCell = document.createElement('td');
                statusCell.textContent = item.status || '—';
                row.appendChild(statusCell);

                const addedCell = document.createElement('td');
                addedCell.textContent = item.addedCount != null ? item.addedCount : '—';
                row.appendChild(addedCell);

                const errorCell = document.createElement('td');
                errorCell.textContent = item.errorMessage || '—';
                row.appendChild(errorCell);

                const updatedCell = document.createElement('td');
                updatedCell.textContent = item.updatedAt || '—';
                row.appendChild(updatedCell);

                tbody.appendChild(row);
            });
        };

        const load = async () => {
            if (!endpoint) {
                return;
            }
            try {
                const response = await fetch(endpoint);
                if (!response.ok) {
                    throw new Error('Не удалось получить историю импорта');
                }
                const payload = await response.json();
                render(payload.content || []);
            } catch (error) {
                console.error('Не удалось обновить историю импорта', error);
            }
        };

        if (reloadButton) {
            reloadButton.addEventListener('click', () => load());
        }

        load();

        return {
            reload: load
        };
    }

    function setupImportHelp() {
        const openButton = document.querySelector('[data-import-help]');
        const modal = document.getElementById('import-help-modal');
        const modalBody = modal?.querySelector('.modal-body');
        const closeButton = modal?.querySelector('[data-modal-close]');
        const content = document.getElementById('import-help-content');

        if (!openButton || !modal || !modalBody || !content) {
            return;
        }

        const showModal = () => {
            modalBody.innerHTML = content.innerHTML;
            modal.hidden = false;
            document.body.classList.add('modal-open');
        };

        const hideModal = () => {
            modal.hidden = true;
            document.body.classList.remove('modal-open');
        };

        openButton.addEventListener('click', showModal);
        closeButton?.addEventListener('click', hideModal);
        modal.addEventListener('click', event => {
            if (event.target === modal) {
                hideModal();
            }
        });
    }

    function setupThemeToggle() {
        const toggle = document.querySelector('[data-theme-toggle]');
        const label = document.querySelector('[data-theme-toggle-label]');
        const icon = document.querySelector('[data-theme-toggle-icon]');
        const storageKey = 'ui-theme-preference';
        const root = document.documentElement;
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

        if (!toggle) {
            return;
        }

        const hasStoredPreference = () => {
            const value = localStorage.getItem(storageKey);
            return value === 'dark' || value === 'light';
        };

        const applyTheme = (theme) => {
            const normalizedTheme = theme === 'dark' ? 'dark' : 'light';
            root.setAttribute('data-theme', normalizedTheme);
            toggle.classList.toggle('is-dark', normalizedTheme === 'dark');
            toggle.setAttribute('aria-pressed', normalizedTheme === 'dark' ? 'true' : 'false');

            if (label) {
                label.textContent = normalizedTheme === 'dark' ? 'Тёмная тема' : 'Светлая тема';
            }

            if (icon) {
                icon.textContent = normalizedTheme === 'dark' ? '🌙' : '🌞';
            }
        };

        const resolveInitialTheme = () => {
            const stored = localStorage.getItem(storageKey);
            if (stored === 'dark' || stored === 'light') {
                return stored;
            }
            return mediaQuery.matches ? 'dark' : 'light';
        };

        applyTheme(resolveInitialTheme());

        toggle.addEventListener('click', () => {
            const current = root.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
            const next = current === 'dark' ? 'light' : 'dark';
            applyTheme(next);
            localStorage.setItem(storageKey, next);
        });

        mediaQuery.addEventListener('change', (event) => {
            if (hasStoredPreference()) {
                return;
            }
            applyTheme(event.matches ? 'dark' : 'light');
        });
    }

    function setupUserMenu() {
        const menu = document.querySelector('[data-user-menu]');
        const toggle = document.querySelector('[data-user-toggle]');
        const panel = document.querySelector('[data-user-panel]');
        if (!menu || !toggle || !panel) {
            return;
        }

        const open = () => {
            panel.hidden = false;
            menu.classList.add('open');
            toggle.setAttribute('aria-expanded', 'true');
        };

        const close = () => {
            panel.hidden = true;
            menu.classList.remove('open');
            toggle.setAttribute('aria-expanded', 'false');
        };

        toggle.addEventListener('click', event => {
            event.stopPropagation();
            if (panel.hidden) {
                open();
            } else {
                close();
            }
        });

        document.addEventListener('click', event => {
            if (!menu.contains(event.target)) {
                close();
            }
        });

        document.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                close();
            }
        });
    }
})();
