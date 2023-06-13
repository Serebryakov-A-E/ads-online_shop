package me.don1ns.adsonlineresaleshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import me.don1ns.adsonlineresaleshop.DTO.*;
import me.don1ns.adsonlineresaleshop.entity.Image;
import me.don1ns.adsonlineresaleshop.entity.User;
import me.don1ns.adsonlineresaleshop.service.AdsService;
import me.don1ns.adsonlineresaleshop.service.CommentService;
import me.don1ns.adsonlineresaleshop.service.ImageService;
import me.don1ns.adsonlineresaleshop.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ads")
@CrossOrigin(value = "http://localhost:3000")
public class AdsController {
    private AdsService adsService;
    private ImageService imageService;
    private CommentService commentService;
    private UserService userService;

    // Получить все объявления
    @Operation(
            summary = "Получить все объявления",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ResponseWrapperAds.class)
                                    )
                            }
                    )
            }
    )
    @GetMapping("/")
    public ResponseEntity<ResponseWrapperAds> getAllAds() {
        return ResponseEntity.ok(adsService.getAllAds());
    }

    // Добавить объявление
    @Operation(
            summary = "Добавить объявления",
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Created",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = AdsDTO.class)
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdsDTO> addAds(@RequestParam("properties") CreateAdsDTO createAds, @RequestParam("image") MultipartFile image, Authentication authentication) {
        User user = userService.checkUserByUsername(authentication.getName());
        if (user != null) {
            try {
                return ResponseEntity.status(HttpStatus.CREATED).body(adsService.adAd(createAds, imageService.uploadImage(image), user));
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    // Получить информацию об объявлении
    @Operation(
            summary = "Получить информацию об объявлении",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = FullAdsDTO.class)
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<FullAdsDTO> getAds(@PathVariable int id) {
        return ResponseEntity.ok(adsService.getAdInfo(id));
    }

    // Удалить объявление
    @Operation(
            summary = "Удалить объявления",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeAd(@PathVariable int id) {
        adsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Обновить информацию об объявлении
    @Operation(
            summary = "Обновить информацию об объявлении",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateAdsDTO.class)
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<AdsDTO> updateAds(@PathVariable int id, @RequestBody CreateAdsDTO createAds) {
        return ResponseEntity.ok(adsService.update(id, createAds));
    }

    // Получить объявления авторизованного пользователя
    @Operation(
            summary = "Получить объявления авторизованного пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ResponseWrapperAds.class)
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ResponseWrapperAds> getAdsMe(Authentication authentication) {
        return ResponseEntity.ok(adsService.getAllUserAds(authentication.getName()));
    }

    // Обновить картинку объявления
    @Operation(
            summary = "Обновить картинку объявления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/octet-stream"
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @PatchMapping("/{id}/image")
    public ResponseEntity<AdsDTO> updateImage(@PathVariable int id, @RequestParam MultipartFile image) {
        try {
            return ResponseEntity.ok(adsService.updateImage(id, imageService.uploadImage(image)));
        } catch (IOException e) {
            return ResponseEntity.status(403).build();
        }
    }

    // Получить комментарии объявления
    @Operation(
            summary = "Получить комментарии объявления",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ResponseWrapperCommentDTO.class)
                                    )
                            }
                    ),

                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("{id}/comments")
    public ResponseEntity<ResponseWrapperCommentDTO> getComments(@PathVariable int id) {
        return ResponseEntity.ok(commentService.getComments(id));
    }

    // Добавить комментарий к объявлению
    @Operation(
            summary = "Добавить комментарий к объявлению",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CommentDTO.class)
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping("{id}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable int id, @RequestBody CreateCommentDTO createCommentDTO, Authentication authentication) {
        return ResponseEntity.ok(commentService.addComment(id, createCommentDTO));
    }

    // Удалить комментарий
    @Operation(
            summary = "Удалить комментарий",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @DeleteMapping("{adId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> deleteComment(@PathVariable int adId, @PathVariable int commentId) {
        commentService.deleteById(adId, commentId);
        return ResponseEntity.ok().build();
    }

    // Обновить комментарий
    @Operation(
            summary = "Обновить комментарий",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CommentDTO.class)
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @PatchMapping("{adId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable int adId, @PathVariable int commentId,
                                                    @RequestBody CommentDTO comment) {
        return ResponseEntity.ok(commentService.updateComment(adId, commentId, comment));
    }

}