class GenresManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/genres';
    }

    init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.loadGenres();
        });
    }

    loadGenres() {
        fetch(this.apiUrl)
            .then(response => response.json())
            .then(genres => {
                const tbody = document.getElementById('genres-table-body');
                if (tbody) {
                    tbody.innerHTML = '';

                    genres.forEach(genre => {
                        const row = this.createGenreRow(genre);
                        tbody.appendChild(row);
                    });
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки жанров:', error);
                this.showError('Ошибка загрузки списка жанров');
            });
    }

    createGenreRow(genre) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <th scope="row">${this.escapeHtml(genre.id)}</th>
            <td class="text-start">
                <a href="/genres/${this.escapeHtml(genre.id)}/details">${this.escapeHtml(genre.name)}</a>
            </td>
            <td>
                <a class="btn btn-outline-info btn-sm me-1" href="/genres/${this.escapeHtml(genre.id)}/edit" type="button">Редактировать</a>
            </td>
            <td>
                <button class="btn btn-outline-info btn-sm" onclick="genresManager.deleteGenre(${this.escapeHtml(genre.id)})">Удалить</button>
            </td>
        `;
        return row;
    }

    deleteGenre(id) {
        if (confirm('Вы уверены, что хотите удалить жанр?')) {
            fetch(`${this.apiUrl}/${id}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        this.loadGenres();
                    } else {
                        throw new Error('Ошибка удаления');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления жанра:', error);
                    this.showError('Ошибка при удалении жанра');
                });
        }
    }

    showError(message) {
        alert(message);
    }

    escapeHtml(text) {
        return super.escapeHtml(text);
    }
}

const genresManager = new GenresManager();
genresManager.init();