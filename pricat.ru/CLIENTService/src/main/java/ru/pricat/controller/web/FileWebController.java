package ru.pricat.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.pricat.model.dto.response.AdminProfileDto;
import ru.pricat.service.ClientService;
import ru.pricat.service.FileService;

/**
 * Web Controller для страницы управления файлами.
 * Обеспечивает отображение страницы с файлами пользователя.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("")
public class FileWebController {

    private final ClientService clientService;

    private final FileService fileService;

    /**
     * Отображает страницу управления файлами.
     * Доступна только для пользователей с isSupplier=true или ролью ADMIN.
     *
     * @param authentication данные аутентификации
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы файлов или редирект при отсутствии доступа
     */
    @PreAuthorize("@fileServiceImpl.canUserUploadFiles(authentication.name)")
    @GetMapping("/files")
    public String showFilesPage(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.debug("Showing files page for user: {}", username);

        if (!fileService.canUserUploadFiles(username)) {
            log.warn("Access denied to files page for user: {}", username);
            return "redirect:/profile?error=access_denied";
        }

        AdminProfileDto profile = clientService.getCurrentUserProfile(username);
        model.addAttribute("user", profile);
        model.addAttribute("companyFolder", profile.companyFolder());

        return "files";
    }
}