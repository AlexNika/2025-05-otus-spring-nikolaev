class GenreViewManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/genres';
        this.genreId = null;
        this.previousUrl = '/genres';
    }

    init(genreId, previousUrl) {
        this.genreId = genreId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            this.loadGenreDetails();
        });
    }

    loadGenreDetails() {
        fetch(`${this.apiUrl}/${this.genreId}`)
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else if (response.status === 404) {
                    throw new Error('Жанр не найден');
                } else {
                    throw new Error('Ошибка загрузки данных');
                }
            })
            .then(genre => {
                this.updateView(genre);
            })
            .catch(error => {
                console.error('Ошибка загрузки жанра:', error);
                this.showError('Жанр не найден');
                this.updateViewWithError('Жанр не найден');
            });
    }

    updateView(genre) {
        const header = document.getElementById('genre-header');
        const name = document.getElementById('genre-name');
        const editLink = document.getElementById('edit-link');

        if (header) {
            header.textContent = `Информационная карточка жанра — #${this.escapeHtml(genre.id)}`;
        }
        if (name) {
            name.textContent = `Название жанра: ${this.escapeHtml(genre.name)}`;
        }
        if (editLink) {
            editLink.href = `/genres/${this.escapeHtml(genre.id)}/edit`;
        }
    }

    updateViewWithError(errorMessage) {
        const header = document.getElementById('genre-header');
        const name = document.getElementById('genre-name');

        if (header) {
            header.textContent = 'Ошибка';
        }
        if (name) {
            name.textContent = errorMessage;
            name.className = 'h6 card-text text-danger';
        }
    }

    deleteGenre() {
        if (confirm('Вы уверены, что хотите удалить жанр?')) {
            fetch(`${this.apiUrl}/${this.genreId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        window.location.href = this.previousUrl;
                    } else if (response.status === 404) {
                        throw new Error('Жанр не найден');
                    } else {
                        throw new Error('Ошибка удаления');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления жанра:', error);
                    alert('Жанр не найден или ошибка при удалении');
                    window.location.href = this.previousUrl;
                });
        }
    }

    showError(message) {
        const nameElement = document.getElementById('genre-name');
        if (nameElement) {
            nameElement.textContent = message;
            nameElement.className = 'h6 card-text text-danger';
        }
    }

    escapeHtml(text) {
        return super.escapeHtml(text);
    }
}

const genreViewManager = new GenreViewManager();