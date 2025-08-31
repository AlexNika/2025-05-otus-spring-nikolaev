class CommentsManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/comments';
        this.bookId = null;
    }

    init(bookId) {
        this.bookId = bookId;
        document.addEventListener('DOMContentLoaded', () => {
            this.loadComments();
        });
    }

    loadComments() {
        fetch(`${this.apiUrl}?bookId=${this.bookId}`)
            .then(response => response.json())
            .then(comments => {
                const tbody = document.getElementById('comments-table-body');
                if (tbody) {
                    tbody.innerHTML = '';

                    if (comments && comments.length > 0) {
                        comments.forEach(comment => {
                            const row = this.createCommentRow(comment);
                            tbody.appendChild(row);
                        });
                    } else {
                        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Комментарии отсутствуют</td></tr>';
                    }
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки комментариев:', error);
                this.showError('Ошибка загрузки списка комментариев');
            });
    }

    createCommentRow(comment) {
        const row = document.createElement('tr');
        row.setAttribute('data-comment-id', comment.id);
        row.innerHTML = `
            <th scope="row">${this.escapeHtml(comment.id)}</th>
            <td class="text-start">
                <a class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                   href="/books/${this.escapeHtml(this.bookId)}/comments/${this.escapeHtml(comment.id)}/details">
                    ${this.escapeHtml(comment.text)}
                </a>
            </td>
            <td>
                <a class="btn btn-outline-info btn-sm me-1" 
                   href="/books/${this.escapeHtml(this.bookId)}/comments/${this.escapeHtml(comment.id)}/edit" 
                   type="button">
                    Редактировать
                </a>
            </td>
            <td>
                <button class="btn btn-outline-info btn-sm delete-comment-btn" 
                        data-comment-id="${this.escapeHtml(comment.id)}" 
                        type="button"
                        onclick="commentsManager.deleteComment(${this.escapeHtml(comment.id)}); return false;">
                    Удалить
                </button>
            </td>
        `;
        return row;
    }

    deleteComment(commentId) {
        if (confirm('Вы уверены, что хотите удалить комментарий?')) {
            fetch(`${this.apiUrl}/${commentId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        this.loadComments();
                    } else {
                        throw new Error('Ошибка удаления');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления комментария:', error);
                    this.showError('Ошибка при удалении комментария');
                });
        }
    }

    showError(message) {
        alert(message);
    }
}

const commentsManager = new CommentsManager();