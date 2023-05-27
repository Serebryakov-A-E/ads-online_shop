package me.don1ns.adsonlineresaleshop.service;

import me.don1ns.adsonlineresaleshop.entity.AdsComment;

public interface AdsCommentService {
    void save(AdsComment adsComment);

    void update(AdsComment adsComment);

    void delete(AdsComment adsComment);
    void deleteById(int id);
    AdsComment getById(int id);
}
