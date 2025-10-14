(function () {
    document.addEventListener('DOMContentLoaded', () => {
        setupPagination();
        setupWebSocket();
    });

    function setupPagination() {
        const pagination = document.querySelector('.pagination');
        if (!pagination) {
            return;
        }
        const page = parseInt(pagination.getAttribute('data-page'), 10) || 0;
        const size = parseInt(pagination.getAttribute('data-size'), 10) || 10;
        const totalPages = parseInt(document.getElementById('bands-table')?.getAttribute('data-total-pages') || '0', 10);
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
        const countEl = document.getElementById('band-count');
        if (!sumEl || typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            return;
        }
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);
        stompClient.debug = () => {};
        stompClient.connect({}, () => {
            const requestSummary = () => stompClient.send('/app/bands/summary', {});
            stompClient.subscribe('/topic/bands', () => requestSummary());
            stompClient.subscribe('/topic/bands-summary', message => {
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
})();
