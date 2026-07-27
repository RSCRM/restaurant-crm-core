package com.restaurant.crm.common.cloudinary;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryImageService {

    /** Upload/replace anh tai publicId (overwrite). Tra ve URL khong expire (secure_url). */
    String upload(String publicId, MultipartFile file);

    /** Xoa asset theo publicId. */
    void delete(String publicId);

    /**
     * Lay URL cua asset. expireSeconds == null -> URL public khong het han (dung de luu DB);
     * co gia tri -> signed URL.
     */
    String getUrl(String publicId, Long expireSeconds);
}
