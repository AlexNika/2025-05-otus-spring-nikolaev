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
            const form = document.getElementById('genre-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveGenre();
                });
            }
        });
    }

    saveGenre() {
        const nameInput = document.getElementById('genreFullNameInput');
        const name = nameInput ? nameInput.value : '';

        this.clearErrors();

        if (!name.trim()) {
            this.showFieldError('name', 'Название жанра не может быть пустым');
            return;
        }

        if (name.trim().length > 255) {
            this.showFieldError('name', 'Название жанра не может быть длиннее 255 символов');
            return;
        }

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ? `${this.apiUrl}/${this.genreId}` : this.apiUrl;

        const data = {
            name: name.trim()
        };

        this.handleFormSubmit(url, method, data, 'name').then(result => {
            if (result) {
                window.location.href = this.previousUrl;
            }
        });
    }
}

const genreFormManager = new GenreFormManager();