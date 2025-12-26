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
            this.updateUI();

            const backLink = document.getElementById('back-link');
            if (backLink) {
                backLink.href = this.previousUrl;
            }

            if (this.isUpdate && this.authorId) {
                this.loadAuthorForEdit();
            }

            const form = document.getElementById('author-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveAuthor();
                });
            }
        });
    }

    updateUI() {
        const titleEl = document.getElementById('form-title');
        const submitBtn = document.getElementById('submit-btn');

        if (titleEl) {
            titleEl.textContent = this.isUpdate ? 'Редактирование автора' : 'Создание нового автора';
        }
        if (submitBtn) {
            submitBtn.textContent = this.isUpdate ? 'Обновить' : 'Сохранить';
        }
    }

    loadAuthorForEdit() {
        fetch(`${this.apiUrl}/${this.authorId}`)
            .then(response => {
                if (!response.ok) throw new Error('Автор не найден');
                return response.json();
            })
            .then(author => {
                document.getElementById('authorFullNameInput').value = author.fullName || '';
                const idInput = document.getElementById('author-id');
                if (idInput) idInput.value = author.id;
            })
            .catch(error => {
                console.error('Ошибка загрузки автора:', error);
                alert('Не удалось загрузить данные автора.');
                window.location.href = this.previousUrl;
            });
    }

    saveAuthor() {
        const fullName = document.getElementById('authorFullNameInput')?.value.trim();

        this.clearErrors();

        if (!fullName) {
            this.showFieldError('fullName', 'ФИО автора не может быть пустым');
            return;
        }

        if (fullName.length > 255) {
            this.showFieldError('fullName', 'ФИО автора не может быть длиннее 255 символов');
            return;
        }

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ? `${this.apiUrl}/${this.authorId}` : this.apiUrl;

        const data = { fullName };

        this.handleFormSubmit(url, method, data, 'fullName').then(result => {
            if (result) {
                window.location.href = this.previousUrl;
            }
        });
    }
}

const authorFormManager = new AuthorFormManager();