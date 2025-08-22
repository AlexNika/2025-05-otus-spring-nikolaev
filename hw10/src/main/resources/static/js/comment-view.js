class CommentViewManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/books';
        this.bookId = null;
        this.commentId = null;
        this.previousUrl = `/books`;
    }

    init(bookId, commentId, previousUrl) {
        this.bookId = bookId;
        this.commentId = commentId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            this.loadCommentDetails();
        });
    }

    loadCommentDetails() {
        fetch(`${this.apiUrl}/${this.bookId}/comments/${this.commentId}`)
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else if (response.status === 404) {
                    throw new Error('Комментарий не найден');
                } else {
                    throw new Error('Ошибка загрузки данных');
                }
            })
            .then(comment => {
                this.updateView(comment);
            })
            .catch(error => {
                console.error('Ошибка загрузки комментария:', error);
                this.showError('Комментарий не найден');
                this.updateViewWithError('Комментарий не найден');
            });
    }

    updateView(comment) {
        const header = document.getElementById('comment-header');
        const text = document.getElementById('comment-text');
        const bookTitle = document.getElementById('book-title');
        const editLink = document.getElementById('edit-link');

        if (header) {
            header.textContent = `Информационная карточка комментария — #${this.escapeHtml(comment.id)}`;
        }
        if (text) {
            text.textContent = `Текст комментария: ${this.escapeHtml(comment.text)}`;
        }
        if (bookTitle) {
            const bookInfo = comment.bookTitle || 'Неизвестная книга';
            bookTitle.textContent = `Книга: ${this.escapeHtml(bookInfo)}`;
        }
        if (editLink) {
            editLink.href = `/books/${this.escapeHtml(this.bookId)}/comments/${this.escapeHtml(comment.id)}/edit`;
        }
    }

    updateViewWithError(errorMessage) {
        const header = document.getElementById('comment-header');
        const text = document.getElementById('comment-text');
        const bookTitle = document.getElementById('book-title');

        if (header) {
            header.textContent = 'Ошибка';
        }
        if (text) {
            text.textContent = errorMessage;
            text.className = 'h6 card-text text-danger';
        }
        if (bookTitle) {
            bookTitle.textContent = '';
        }
    }

    deleteComment() {
        if (confirm('Вы уверены, что хотите удалить комментарий?')) {
            fetch(`${this.apiUrl}/${this.bookId}/comments/${this.commentId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        window.location.href = `/books/${this.bookId}/comments`;
                    } else {
                        throw new Error('Ошибка удаления');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления комментария:', error);
                    alert('Комментарий не найден или ошибка при удалении');
                    window.location.href = `/books/${this.bookId}/comments`;
                });
        }
    }

    showError(message) {
        const textElement = document.getElementById('comment-text');
        if (textElement) {
            textElement.textContent = message;
            textElement.className = 'h6 card-text text-danger';
        }
    }

    escapeHtml(text) {
        return super.escapeHtml(text);
    }
}

const commentViewManager = new CommentViewManager();