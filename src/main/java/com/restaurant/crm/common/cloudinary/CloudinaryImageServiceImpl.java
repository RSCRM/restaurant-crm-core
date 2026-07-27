package com.restaurant.crm.common.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryImageServiceImpl implements CloudinaryImageService {

    Cloudinary cloudinary;

    @Override
    public String upload(String publicId, MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "invalidate", true,
                    "resource_type", "image"
            ));
            return String.valueOf(result.get("secure_url"));
        } catch (IOException e) {
            log.error("Cloudinary upload failed for {}", publicId, e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "invalidate", true,
                    "resource_type", "image"
            ));
        } catch (IOException e) {
            log.error("Cloudinary delete failed for {}", publicId, e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public String getUrl(String publicId, Long expireSeconds) {
        if (expireSeconds == null) {
            // public delivery URL, khong het han -> luu DB
            return cloudinary.url().secure(true).resourceType("image").generate(publicId);
        }
        // signed URL. Luu y: expiry theo thoi gian can Cloudinary auth-token (add-on) -> follow-up.
        return cloudinary.url().secure(true).resourceType("image").signed(true).generate(publicId);
    }
}
