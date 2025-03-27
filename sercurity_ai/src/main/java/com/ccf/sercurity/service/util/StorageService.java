package com.ccf.sercurity.service.util;

import io.minio.errors.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface StorageService {
    /**
     * 创建桶/文件夹
     */
    void createBucket(String bucket);

    /**
     * 上传一个文件
     */
    void uploadFile(InputStream stream, String bucket, String objectName) throws Exception;

    /**
     * 列出所有的桶
     */
    List<String> listBuckets();

    /**
     * 下载一个文件
     */
    InputStream download(String bucket, String objectName) throws Exception;

    /**
     * 删除一个对象
     */
    void deleteObject(String bucket, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * 上传文件夹
     */
    void uploadFolder(String bucketName, File folder, String filePath) throws MinioException;

}
