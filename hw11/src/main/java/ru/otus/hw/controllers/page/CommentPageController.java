package ru.otus.hw.controllers.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.REFERER;

@Controller
@RequiredArgsConstructor
public class CommentPageController {

    @GetMapping("/books/{bookId}/comments")
    public String getAllCommentsByBookPage(@PathVariable Long bookId,
                                           @RequestHeader(value = REFERER, required = false) String referrer,
                                           Model model) {
        model.addAttribute("bookId", bookId);
        model.addAttribute("previousUrl", referrer);
        return "comments";
    }

    @GetMapping("/books/{bookId}/comments/{commentId}/details")
    public String getCommentDetailsPage(@PathVariable Long bookId,
                                        @PathVariable Long commentId,
                                        @RequestHeader(value = REFERER, required = false) String referrer,
                                        Model model) {
        model.addAttribute("bookId", bookId);
        model.addAttribute("commentId", commentId);
        model.addAttribute("previousUrl", referrer);
        return "comment-view";
    }

    @GetMapping("/books/{bookId}/comments/add")
    public String showAddCommentForm(@PathVariable Long bookId,
                                     @RequestHeader(value = REFERER, required = false) String referrer,
                                     Model model) {
        model.addAttribute("bookId", bookId);
        model.addAttribute("previousUrl", referrer);
        return "comment-upsert";
    }

    @GetMapping("/books/{bookId}/comments/{commentId}/edit")
    public String showEditCommentForm(@PathVariable Long bookId,
                                      @PathVariable Long commentId,
                                      @RequestHeader(value = REFERER, required = false) String referrer,
                                      Model model) {
        model.addAttribute("bookId", bookId);
        model.addAttribute("commentId", commentId);
        model.addAttribute("isUpdate", true);
        model.addAttribute("previousUrl", referrer);
        return "comment-upsert";
    }
}