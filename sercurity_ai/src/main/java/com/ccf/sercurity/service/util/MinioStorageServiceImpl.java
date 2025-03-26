package com.ccf.sercurity.service.util;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;

    @Override
    public void createBucket(String bucket) {
        System.out.println(bucket);
    }

    @Override
    public void uploadFile(InputStream stream, String bucket, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName)
                .stream(stream, -1, 10485760).build());
    }

    @Override
    public List<String> listBuckets() {
        return List.of();
    }

    @Override
    public InputStream download(String bucket, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build()
        );
    }

    @Override
    public void deleteObject(String bucket, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder().bucket(bucket).object(objectName).build()
        );
    }

    @Override
    public void uploadFolder(String bucketName, File folder, String filePath) throws MinioException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(filePath + "/").stream(
                            new ByteArrayInputStream(new byte[]{}), 0, -1
                    ).build()
            );
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    uploadFile(fileInputStream, bucketName, filePath + "/" + file.getName());
                    fileInputStream.close();
                }
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            System.err.println("Error occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
