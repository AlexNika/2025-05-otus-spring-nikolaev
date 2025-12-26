package ru.otus.hw.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentMinDto;
import ru.otus.hw.models.Comment;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    CommentDto toCommentDto(Comment comment);

    CommentMinDto toCommentMinDto(Comment comment);
}