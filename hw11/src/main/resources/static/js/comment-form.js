class CommentFormManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/comments';
        this.isUpdate = false;
        this.bookId = null;
        this.commentId = null;
        this.previousUrl = `/books`;
    }

    init(isUpdate, bookId, commentId, previousUrl) {
        this.isUpdate = isUpdate;
        this.bookId = bookId;
        this.commentId = commentId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            this.updateUI();

            const backLink = document.getElementById('back-link');
            if (backLink) {
                backLink.href = this.isUpdate
                    ? `/books/${this.bookId}/comments/${this.commentId}/details`
                    : `/books/${this.bookId}/comments`;
            }

            if (this.isUpdate && this.commentId) {
                this.loadCommentForEdit();
            }

            const form = document.getElementById('comment-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveComment();
                });
            }
        });
    }

    updateUI() {
        const titleEl = document.getElementById('form-title');
        const submitBtn = document.getElementById('submit-btn');

        if (titleEl) {
            titleEl.textContent = this.isUpdate
                ? 'Редактирование комментария'
                : 'Создание нового комментария';
        }
        if (submitBtn) {
            submitBtn.textContent = this.isUpdate ? 'Обновить' : 'Сохранить';
        }
    }

    loadCommentForEdit() {
        fetch(`${this.apiUrl}/${this.commentId}`)
            .then(response => {
                if (!response.ok) throw new Error('Комментарий не найден');
                return response.json();
            })
            .then(comment => {
                document.getElementById('commentTextInput').value = comment.text || '';
            })
            .catch(error => {
                console.error('Ошибка загрузки комментария:', error);
                alert('Не удалось загрузить данные комментария.');
                window.location.href = `/books/${this.bookId}/comments`;
            });
    }

    saveComment() {
        const text = document.getElementById('commentTextInput')?.value.trim();

        this.clearErrors();

        if (!text) {
            this.showFieldError('text', 'Текст комментария не может быть пустым');
            return;
        }

        if (text.length > 255) {
            this.showFieldError('text', 'Текст комментария не может быть длиннее 255 символов');
            return;
        }

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ? `${this.apiUrl}/${this.commentId}` : this.apiUrl;

        const data = {
            text,
            bookId: this.bookId
        };

        this.handleFormSubmit(url, method, data, 'text').then(result => {
            if (result) {
                window.location.href = `/books/${this.bookId}/comments`;
            }
        });
    }
}

const commentFormManager = new CommentFormManager();