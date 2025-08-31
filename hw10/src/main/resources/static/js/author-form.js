class AuthorFormManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/authors';
        this.isUpdate = false;
        this.authorId = null;
        this.previousUrl = '/authors';
    }

    init(isUpdate, authorId, previousUrl) {
        this.isUpdate = isUpdate;
        this.authorId = authorId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            const form = document.getElementById('author-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveAuthor();
                });
            }
        });
    }

    saveAuthor() {
        const fullNameInput = document.getElementById('authorFullNameInput');
        const fullName = fullNameInput ? fullNameInput.value : '';

        this.clearErrors();

        if (!fullName.trim()) {
            this.showFieldError('fullName', 'ФИО автора не может быть пустым');
            return;
        }

        if (fullName.trim().length > 255) {
            this.showFieldError('fullName', 'ФИО автора не может быть длиннее 255 символов');
            return;
        }

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ? `${this.apiUrl}/${this.authorId}` : this.apiUrl;

        const data = {
            fullName: fullName.trim()
        };

        this.handleFormSubmit(url, method, data, 'fullName').then(result => {
            if (result) {
                window.location.href = this.previousUrl;
            }
        });
    }
}

const authorFormManager = new AuthorFormManager();