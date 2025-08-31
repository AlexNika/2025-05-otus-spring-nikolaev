class AuthorViewManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/authors';
        this.authorId = null;
        this.previousUrl = '/authors';
    }

    init(authorId, previousUrl) {
        this.authorId = authorId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            this.loadAuthorDetails();
            this.loadAuthorBooks();
        });
    }

    loadAuthorDetails() {
        fetch(`${this.apiUrl}/${this.authorId}`)
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else if (response.status === 404) {
                    throw new Error('Автор не найден');
                } else {
                    throw new Error('Ошибка загрузки данных');
                }
            })
            .then(author => {
                this.updateAuthorView(author);
            })
            .catch(error => {
                console.error('Ошибка загрузки автора:', error);
                this.showError('Автор не найден');
                this.updateAuthorViewWithError('Автор не найден');
            });
    }

    loadAuthorBooks() {
        fetch(`${this.apiUrl}/${this.authorId}/books`)
            .then(response => response.json())
            .then(books => {
                this.updateBooksView(books);
            })
            .catch(error => {
                console.error('Ошибка загрузки книг автора:', error);
                this.updateBooksView([]);
            });
    }

    updateAuthorView(author) {
        const header = document.getElementById('author-header');
        const fullName = document.getElementById('author-fullname');
        const editLink = document.getElementById('edit-link');

        if (header) {
            header.textContent = `Информационная карточка автора — #${this.escapeHtml(author.id)}`;
        }
        if (fullName) {
            fullName.textContent = `ФИО автора: ${this.escapeHtml(author.fullName)}`;
        }
        if (editLink) {
            editLink.href = `/authors/${this.escapeHtml(author.id)}/edit`;
        }
    }

    updateAuthorViewWithError(errorMessage) {
        const header = document.getElementById('author-header');
        const fullName = document.getElementById('author-fullname');

        if (header) {
            header.textContent = 'Ошибка';
        }
        if (fullName) {
            fullName.textContent = errorMessage;
            fullName.className = 'h6 card-text text-danger';
        }
    }

    updateBooksView(books) {
        const booksList = document.getElementById('author-books');
        if (booksList) {
            if (books && books.length > 0) {
                booksList.innerHTML = books.map(book =>
                    `<li><a class="ms-2" href="/books/${this.escapeHtml(book.id)}/details">${this.escapeHtml(book.title)}</a></li>`
                ).join('');
            } else {
                booksList.innerHTML = '<li>Нет книг</li>';
            }
        }
    }

    deleteAuthor() {
        if (confirm('Вы уверены, что хотите удалить автора?')) {
            fetch(`${this.apiUrl}/${this.authorId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        window.location.href = this.previousUrl;
                    } else if (response.status === 404) {
                        throw new Error('Автор не найден');
                    } else {
                        throw new Error('Ошибка удаления');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления автора:', error);
                    alert('Автор не найден или ошибка при удалении');
                    window.location.href = this.previousUrl;
                });
        }
    }

    showError(message) {
        const fullNameElement = document.getElementById('author-fullname');
        if (fullNameElement) {
            fullNameElement.textContent = message;
            fullNameElement.className = 'h6 card-text text-danger';
        }
    }

    escapeHtml(text) {
        return super.escapeHtml(text);
    }
}

const authorViewManager = new AuthorViewManager();