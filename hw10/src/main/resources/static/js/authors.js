class AuthorsManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/authors';
        this.booksApiUrl = '/api/v1/books';
    }

    init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.loadAuthors();
        });
    }

    loadAuthors() {
        fetch(this.apiUrl)
            .then(response => response.json())
            .then(authors => {
                const tbody = document.getElementById('authors-table-body');
                if (tbody) {
                    tbody.innerHTML = '';

                    const promises = authors.map(author =>
                        this.loadAuthorBooksWithDetails(author)
                    );

                    Promise.all(promises).then(results => {
                        results.forEach(result => {
                            const row = this.createAuthorRow(result.author, result.books);
                            tbody.appendChild(row);
                        });
                    });
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки авторов:', error);
                this.showError('Ошибка загрузки списка авторов');
            });
    }

    loadAuthorBooksWithDetails(author) {
        return fetch(`${this.apiUrl}/${author.id}/books`)
            .then(response => response.json())
            .then(books => ({author, books}))
            .catch(() => ({author, books: []}));
    }

    createAuthorRow(author, books) {
        const row = document.createElement('tr');
        const booksHtml = books.map(book =>
            `<div><a href="/books/${this.escapeHtml(book.id)}/details">${this.escapeHtml(book.title)}</a></div>`
        ).join('');

        row.innerHTML = `
                <th scope="row">${this.escapeHtml(author.id)}</th>
                <td class="text-start">
                    <a href="/authors/${this.escapeHtml(author.id)}/details">${this.escapeHtml(author.fullName)}</a>
                </td>
                <td class="text-start">
                    ${booksHtml || '<div>Нет книг</div>'}
                </td>
            `;
        return row;
    }

    deleteAuthor(id) {
        if (confirm('Вы уверены, что хотите удалить автора?')) {
            fetch(`${this.apiUrl}/${id}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        this.loadAuthors();
                    } else {
                        throw new Error('Ошибка удаления');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления автора:', error);
                    this.showError('Ошибка при удалении автора');
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

const authorsManager = new AuthorsManager();
authorsManager.init();