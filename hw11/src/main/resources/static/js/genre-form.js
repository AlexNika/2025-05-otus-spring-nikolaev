class GenreFormManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/genres';
        this.isUpdate = false;
        this.genreId = null;
        this.previousUrl = '/genres';
    }

    init(isUpdate, genreId, previousUrl) {
        this.isUpdate = isUpdate;
        this.genreId = genreId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            this.updateUI();

            const backLink = document.getElementById('back-link');
            if (backLink) {
                backLink.href = this.previousUrl;
            }

            if (this.isUpdate && this.genreId) {
                this.loadGenreForEdit();
            }

            const form = document.getElementById('genre-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveGenre();
                });
            }
        });
    }

    updateUI() {
        const titleEl = document.getElementById('form-title');
        const submitBtn = document.getElementById('submit-btn');

        if (titleEl) {
            titleEl.textContent = this.isUpdate ? 'Редактирование жанра' : 'Создание нового жанра';
        }
        if (submitBtn) {
            submitBtn.textContent = this.isUpdate ? 'Обновить' : 'Сохранить';
        }
    }

    loadGenreForEdit() {
        fetch(`${this.apiUrl}/${this.genreId}`)
            .then(response => {
                if (!response.ok) throw new Error('Жанр не найден');
                return response.json();
            })
            .then(genre => {
                document.getElementById('genreFullNameInput').value = genre.name || '';
                const idInput = document.getElementById('genre-id');
                if (idInput) idInput.value = genre.id;
            })
            .catch(error => {
                console.error('Ошибка загрузки жанра:', error);
                alert('Не удалось загрузить данные жанра.');
                window.location.href = this.previousUrl;
            });
    }

    saveGenre() {
        const name = document.getElementById('genreFullNameInput')?.value.trim();

        this.clearErrors();

        if (!name) {
            this.showFieldError('name', 'Название жанра не может быть пустым');
            return;
        }

        if (name.length > 255) {
            this.showFieldError('name', 'Название жанра не может быть длиннее 255 символов');
            return;
        }

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ? `${this.apiUrl}/${this.genreId}` : this.apiUrl;

        const data = { name };

        this.handleFormSubmit(url, method, data, 'name').then(result => {
            if (result) {
                window.location.href = this.previousUrl;
            }
        });
    }
}

const genreFormManager = new GenreFormManager();