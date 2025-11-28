package ru.pricat.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.pricat.model.dto.response.FileResponseDto;
import ru.pricat.service.FileService;

import static ru.pricat.util.AppConstants.API_V1_CLIENT_PATH;

/**
 * REST Controller для работы с файлами через AJAX.
 * Обеспечивает API для загрузки и получения файлов.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(API_V1_CLIENT_PATH + "/files")
public class FileRestController {

    private final FileService fileService;

    /**
     * Загружает файл прайс-листа для текущего пользователя.
     *
     * @param file файл для загрузки
     * @param companyFolder папка компании в S3
     * @param authentication данные аутентификации
     * @return ResponseEntity с информацией о загруженном файле
     */
    @PostMapping("/upload")
    public ResponseEntity<FileResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyFolder") String companyFolder,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("REST: Uploading file for user: {}, original filename: {}", username, file.getOriginalFilename());

        FileResponseDto response = fileService.uploadFile(username, file, companyFolder);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает страницу с файлами текущего пользователя.
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @param authentication данные аутентификации
     * @return страница с файлами пользователя
     */
    @GetMapping
    public ResponseEntity<Page<FileResponseDto>> getUserFiles(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication) {

        String username = authentication.getName();
        log.debug("REST: Getting files for user: {}, page: {}, size: {}", username, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadDate"));
        Page<FileResponseDto> files = fileService.getUserFiles(username, pageable);
        return ResponseEntity.ok(files);
    }

    /**
     * Проверяет, может ли текущий пользователь загружать файлы.
     *
     * @param authentication данные аутентификации
     * @return true если пользователь может загружать файлы, иначе false
     */
    @GetMapping("/can-upload")
    public ResponseEntity<Boolean> canUserUploadFiles(Authentication authentication) {
        String username = authentication.getName();
        boolean canUpload = fileService.canUserUploadFiles(username);
        return ResponseEntity.ok(canUpload);
    }
}
