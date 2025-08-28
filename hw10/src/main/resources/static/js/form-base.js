class FormBase {
    clearErrors() {
        const errorMessage = document.getElementById('error-message');
        const errorElements = document.querySelectorAll('.invalid-feedback');
        errorElements.forEach(element => {
            element.textContent = '';
            element.classList.remove('d-block');
        });

        if (errorMessage) {
            errorMessage.classList.add('d-none');
        }
    }

    showFieldError(fieldName, message) {
        const errorElement = document.getElementById(`${fieldName}-error`) ||
            document.getElementById('error-message');
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.remove('d-none');
            if (!errorElement.classList.contains('d-block')) {
                errorElement.classList.add('d-block');
            }
        } else {
            alert(message);
        }
    }

    escapeHtml(text) {
        if (typeof text === 'number') return text;
        const map = {
            '&': '&amp;',
            '<': '<',
            '>': '>',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    async handleFormSubmit(url, method, data, fieldName) {
        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                return await response.json();
            }

            let errorData;
            try {
                errorData = await response.json();
            } catch (e) {
                errorData = {message: 'Ошибка сервера'};
            }

            if (response.status === 400 && errorData.errors) {
                Object.keys(errorData.errors).forEach(field => {
                    this.showFieldError(field, errorData.errors[field]);
                });
                return null;
            } else if (errorData && errorData.message) {
                this.showFieldError(fieldName, errorData.message);
                return null;
            } else {
                this.showFieldError(fieldName, 'Ошибка при сохранении');
                return null;
            }
        } catch (error) {
            console.error('Ошибка обработки запроса:', error);
            this.showFieldError(fieldName, 'Ошибка при сохранении');
            return null;
        }
    }
}