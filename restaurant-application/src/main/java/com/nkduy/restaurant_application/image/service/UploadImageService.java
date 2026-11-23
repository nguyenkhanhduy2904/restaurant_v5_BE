package com.nkduy.restaurant_application.image.service;

import com.nkduy.restaurant_application.image.dto.request.CompleteUploadRequest;
import com.nkduy.restaurant_application.image.dto.request.PresignedUploadRequest;
import com.nkduy.restaurant_application.image.dto.request.UploadFileRequest;
import com.nkduy.restaurant_application.image.dto.response.CompleteUploadItem;
import com.nkduy.restaurant_application.image.dto.response.CompleteUploadResponse;
import com.nkduy.restaurant_application.image.dto.response.PresignedUploadItem;
import com.nkduy.restaurant_application.image.dto.response.PresignedUploadResponse;
import com.nkduy.restaurant_application.image.entity.Upload;
import com.nkduy.restaurant_application.image.entity.UploadStatus;
import com.nkduy.restaurant_application.image.repository.UploadRepository;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadImageService {
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final UploadRepository uploadRepository;
    private final UserRepository userRepository;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    private static final Duration URL_EXPIRATION =
            Duration.ofMinutes(10);

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );



    @Transactional
    public PresignedUploadResponse requestProductUpload(String id, PresignedUploadRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
        return new PresignedUploadResponse(
                request.files().stream()
                        .map(fileRequest -> generatePresignedUploadItem(user, fileRequest, "product"))
                        .toList()
        );
    }

    private PresignedUploadItem generatePresignedUploadItem(User user, UploadFileRequest fileRequest, String type) {

        //Validate file type
        String extension = ALLOWED_TYPES.get(fileRequest.contentType());

        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported image type: " + fileRequest.contentType() + " of file named: "+ fileRequest.fileName()
            );
        }
        // Validate file size
        if (fileRequest.fileSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File named: " + fileRequest.fileName() +" exceeds maximum size of 5 MB"
            );
        }

        // Generate unique object key
        String objectKey = type + "/"
                + UUID.randomUUID()
                + "." + extension;

        // 4. Define upload operation
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(fileRequest.contentType())
                .build();

        Instant expiresAt = Instant.now().plus(URL_EXPIRATION);

        // 5. Generate presigned URL
        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(URL_EXPIRATION)
                        .putObjectRequest(putRequest)
                        .build();

        String uploadUrl = s3Presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();

        Upload savedUpload = uploadRepository.save(
                Upload.builder()

                        .objectKey(objectKey)
                        .contentType(fileRequest.contentType())
                        .filesize(fileRequest.fileSize())
                        .expiresAt(expiresAt)
                        .user(user)
                        .build()
        );


        return new PresignedUploadItem(
                fileRequest.clientId(),
                savedUpload.getId(),
                uploadUrl,
                savedUpload.getObjectKey(),
                "PUT",
                savedUpload.getExpiresAt()


        );

    }

    @Transactional
    public CompleteUploadResponse confirmUpload(String id, CompleteUploadRequest request) {

        List<CompleteUploadItem> results = request.uploadIds()
                .stream()
                .map(uploadId -> confirmSingleUpload(id, uploadId))
                .toList();

        return new CompleteUploadResponse(results);

    }

    private CompleteUploadItem confirmSingleUpload(String userId, String uploadId){
        //check exist
        Upload upload = uploadRepository.findById(uploadId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found"));


        //check ownership
        if(!upload.getUser().getId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this upload");
        }


        //idempotency(already confirmed)
        if(upload.getStatus()== UploadStatus.UPLOADED){
            return new CompleteUploadItem(
                    uploadId,
                    upload.getStatus(),
                    "Upload already completed"
            );
        }
        if (upload.getStatus() != UploadStatus.PENDING) {
            return new CompleteUploadItem(
                    uploadId,
                    upload.getStatus(),
                    "Upload cannot be completed in its current state"
            );
        }
        // Check expiration
        if (Instant.now().isAfter(upload.getExpiresAt())) {
            upload.setStatus(UploadStatus.EXPIRED);
            uploadRepository.save(upload);

            return new CompleteUploadItem(
                    uploadId,
                    UploadStatus.EXPIRED,
                    "Upload URL has expired"
            );
        }

        // Verify object in R2
        HeadObjectResponse head = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(upload.getObjectKey())
                        .build()
        );
        // Verify actual file size
        if (head.contentLength() <= 0 ||
                head.contentLength() > MAX_FILE_SIZE ||
                !head.contentLength().equals(upload.getFilesize())) {

            upload.setStatus(UploadStatus.FAILED);
            uploadRepository.save(upload);

            return new CompleteUploadItem(
                    uploadId,
                    UploadStatus.FAILED,
                    "Uploaded file has an invalid size"
            );
        }

        // Verify content type
        if (!upload.getContentType().equals(head.contentType())) {
            upload.setStatus(UploadStatus.FAILED);
            uploadRepository.save(upload);

            return new CompleteUploadItem(
                    uploadId,
                    UploadStatus.FAILED,
                    "Uploaded file has an invalid content type"
            );
        }
        // Mark as uploaded
        upload.setStatus(UploadStatus.UPLOADED);
        uploadRepository.save(upload);

        return new CompleteUploadItem(
                uploadId,
                UploadStatus.UPLOADED,
                "Upload completed successfully"
        );

    }

    public String getImageObjectKey(String uploadId){
        return uploadRepository.findById(uploadId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found")).getObjectKey();
    }

    public List<Upload> getUploads(List<String> uploadIds) {
        List<Upload> uploads = uploadRepository.findAllById(uploadIds);

        if (uploads.size() != uploadIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "One or more uploads not found"
            );
        }

        return uploads;
    }
    public Upload changeUploadStatus(Upload upload, UploadStatus status){
        upload.setStatus(status);
        return upload;
    }


}
