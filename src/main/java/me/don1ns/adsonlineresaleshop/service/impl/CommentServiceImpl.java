package me.don1ns.adsonlineresaleshop.service.impl;

import me.don1ns.adsonlineresaleshop.DTO.CommentDTO;
import me.don1ns.adsonlineresaleshop.DTO.ResponseWrapperCommentDTO;
import me.don1ns.adsonlineresaleshop.entity.Ads;
import me.don1ns.adsonlineresaleshop.entity.Comment;
import me.don1ns.adsonlineresaleshop.entity.User;
import me.don1ns.adsonlineresaleshop.exception.CommentNotFoundException;
import me.don1ns.adsonlineresaleshop.mapper.CommentMapper;
import me.don1ns.adsonlineresaleshop.repository.AdsRepository;
import me.don1ns.adsonlineresaleshop.repository.CommentRepository;
import me.don1ns.adsonlineresaleshop.security.SecurityUtils;
import me.don1ns.adsonlineresaleshop.service.AdsService;
import me.don1ns.adsonlineresaleshop.service.CommentService;
import me.don1ns.adsonlineresaleshop.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.webjars.NotFoundException;

import java.util.List;

import static me.don1ns.adsonlineresaleshop.constant.Constant.COMMENT_NOT_BELONG_AD_MSG;
import static me.don1ns.adsonlineresaleshop.constant.Constant.COMMENT_NOT_FOUND_MSG;

public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final AdsRepository adsRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;

    public CommentServiceImpl(CommentRepository commentRepository, AdsRepository adsRepository, AdsService adsService, CommentMapper commentMapper, UserService userService) {
        this.commentRepository = commentRepository;
        this.adsRepository = adsRepository;
        this.commentMapper = commentMapper;
        this.userService = userService;
    }

    public ResponseWrapperCommentDTO getComments(Integer id, UserDetails currentUser) {
        List<CommentDTO> comments = commentRepository.findByAdsId(id).stream()
                .map(comment -> commentMapper.toCommentDto(comment))
                .toList();
        return new ResponseWrapperCommentDTO(comments.size(), comments);
    }

    @Override
    public CommentDTO addComment(Integer id, Comment commentDto, Authentication authentication) {
        Ads ads = adsRepository.findById(id).orElseThrow();
        User user = userService.checkUserByUsername(authentication.getName());

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setAds(ads);
        comment.setCreatedAt(System.currentTimeMillis());
        comment.setText(commentDto.getText());

        comment = commentRepository.save(comment);

        return commentMapper.toCommentDto(comment);
    }

    @Override
    public CommentDTO updateComment(Integer adId, Integer commentId, CommentDTO commentDTO, Authentication authentication) {
        SecurityUtils.checkPermissionToAdsComment(commentMapper.toAdsComment(commentDTO),
                userService.checkUserByUsername(authentication.getName()));
        if (commentRepository.findById(commentId).isPresent()) {
            if (commentRepository.findById(commentId).get().getId() != adId) {
                throw new NotFoundException(COMMENT_NOT_BELONG_AD_MSG);
            }
        } else {
            throw new CommentNotFoundException(COMMENT_NOT_FOUND_MSG);
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        comment.setCreatedAt(System.currentTimeMillis());
        comment.setText(commentDTO.getText());
        commentRepository.save(comment);
        return commentMapper.toCommentDto(comment);
    }


    @Override
    public boolean deleteComment(Integer adId, Integer commentId, Authentication authentication) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MSG.formatted(commentId)));
        if (comment.getAds().getId() != adId) {
            throw new NotFoundException(COMMENT_NOT_BELONG_AD_MSG);
        }
        SecurityUtils.checkPermissionToAdsComment(comment, userService.checkUserByUsername(authentication.getName()));
        commentRepository.delete(comment);
        return true;
    }
}
