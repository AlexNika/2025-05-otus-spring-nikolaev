class BooksManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/books';
    }

    init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.loadBooks();
        });
    }

    loadBooks() {
        fetch(this.apiUrl)
            .then(response => response.json())
            .then(books => {
                const tbody = document.getElementById('books-table-body');
                if (tbody) {
                    tbody.innerHTML = '';

                    if (books && books.length > 0) {
                        books.forEach(book => {
                            const row = this.createBookRow(book);
                            tbody.appendChild(row);
                        });
                    } else {
                        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Книги отсутствуют</td></tr>';
                    }
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки книг:', error);
                this.showError('Ошибка загрузки списка книг');
            });
    }

    createBookRow(book) {
        const row = document.createElement('tr');
        row.setAttribute('data-book-id', book.id);

        const genresHtml = book.genres && book.genres.length > 0
            ? book.genres.map(genre =>
                `<div><a href="/genres/${this.escapeHtml(genre.id)}/details">${this.escapeHtml(genre.name)}</a></div>`
            ).join('')
            : '<div>Нет жанров</div>';

        row.innerHTML = `
            <th scope="row">${this.escapeHtml(book.id)}</th>
            <td class="text-start">
                <a class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                   href="/books/${this.escapeHtml(book.id)}/details">
                    ${this.escapeHtml(book.title)}
                </a>
            </td>
            <td class="text-start">
                <a class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                   href="/authors/${this.escapeHtml(book.author.id)}/details">
                    ${this.escapeHtml(book.author.fullName)}
                </a>
            </td>
            <td class="text-start">
                ${genresHtml}
            </td>
            <td class="text-start">
                <a class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                   href="/books/${this.escapeHtml(book.id)}/comments">
                    Список комментариев
                </a>
            </td>
        `;
        return row;
    }

    showError(message) {
        alert(message);
    }
}

const booksManager = new BooksManager();