class CommentFormManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/books';
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
            const form = document.getElementById('comment-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveComment();
                });
            }
        });
    }

    saveComment() {
        const textInput = document.getElementById('commentTextInput');
        const text = textInput ? textInput.value : '';

        this.clearErrors();

        if (!text.trim()) {
            this.showFieldError('text', 'Текст комментария не может быть пустым');
            return;
        }

        if (text.trim().length > 255) {
            this.showFieldError('text', 'Текст комментария не может быть длиннее 255 символов');
            return;
        }

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ?
            `${this.apiUrl}/${this.bookId}/comments/${this.commentId}` :
            `${this.apiUrl}/${this.bookId}/comments`;

        const data = {
            text: text.trim()
        };

        this.handleFormSubmit(url, method, data, 'text').then(result => {
            if (result) {
                window.location.href = `/books/${this.bookId}/comments`;
            }
        });
    }
}

const commentFormManager = new CommentFormManager();