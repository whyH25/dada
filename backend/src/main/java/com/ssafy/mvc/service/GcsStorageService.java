package com.ssafy.mvc.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// 이력서/포트폴리오 원본 파일을 GCS 비공개 버킷에 저장하고 signed URL로만 접근 허용
@Service
@RequiredArgsConstructor
public class GcsStorageService {

    private final Storage storage;

    @Value("${gcs.bucket}")
    private String bucket;

    // 파일을 prefix(resume/portfolio) 하위에 UUID로 저장하고 내부 식별용 gs:// 경로를 반환
    public String upload(String prefix, MultipartFile file) throws IOException {
        String objectName = prefix + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, objectName))
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        return "gs://" + bucket + "/" + objectName;
    }

    // gs:// 경로를 일정 시간만 유효한 다운로드 URL로 변환 (버킷은 비공개 유지)
    public String generateSignedUrl(String gsUri, Duration expiry) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, toObjectName(gsUri))).build();
        URL url = storage.signUrl(blobInfo, expiry.toMinutes(), TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature());
        return url.toString();
    }

    // 마이페이지에서 원본 삭제 시 GCS 객체도 함께 제거
    public void delete(String gsUri) {
        if (gsUri == null || gsUri.isBlank()) return;
        storage.delete(BlobId.of(bucket, toObjectName(gsUri)));
    }

    private String toObjectName(String gsUri) {
        return gsUri.substring(("gs://" + bucket + "/").length());
    }
}
