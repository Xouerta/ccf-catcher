package com.ccf.sercurity.service;

import com.ccf.sercurity.error.ErrorEnum;
import com.ccf.sercurity.error.PlatformException;
import com.ccf.sercurity.model.FileInfo;
import com.ccf.sercurity.repository.FileRepository;
import com.ccf.sercurity.service.util.StorageService;
import com.ccf.sercurity.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

/**
 * 文件服务类
 * 负责处理文件上传、存储和检索相关操作
 */
@Service
public class FileService {

    private final static String fileBuketName = "file";
    private final static String blank = "/";

    /**
     * 文件信息仓库接口
     */
    private final FileRepository fileRepository;


    private final StorageService storageService;

    /**
     * 构造函数，注入文件仓库
     *
     * @param fileRepository 文件仓库接口
     */
    @Autowired
    public FileService(FileRepository fileRepository, StorageService storageService) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
    }

    /**
     * 保存上传的文件并记录文件信息
     *
     * @param file 上传的文件对象
     * @return 保存的文件信息对象
     * @throws IOException 如果文件存储过程中发生I/O错误
     */
    public FileInfo saveFile(MultipartFile file, String userId) throws Exception {

        // 生成唯一文件名
        String fileName = file.getOriginalFilename();
        String uniqueFileName = userId + blank + fileName;

        // 创建文件信息对象
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(fileName);
        fileInfo.setStoredName(uniqueFileName);
        fileInfo.setUploadTime(new Date());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setContentType(file.getContentType());
        fileInfo.setFilePath(userId + file.getOriginalFilename());
        fileInfo.setMalicious(false);
        fileInfo.setUploadFileUserId(userId);

        // 保存到ES
        FileInfo info = fileRepository.save(fileInfo);
        // 保存文件到Minio
        storageService.uploadFile(file.getInputStream(), fileBuketName, userId + blank + info.getId() + '_' + file.getOriginalFilename());
        return info;
    }

    public FileInfo updateFileInfoById(FileInfo file) {
        fileRepository.findById(file.getId()).orElseThrow(() ->
                new PlatformException(ErrorEnum.NET_ERROR));
        return fileRepository.save(file);
    }

    /**
     * 根据ID查询文件信息
     *
     * @param id 文件ID
     * @return 文件信息对象
     * @throws RuntimeException 如果文件不存在
     */
    public FileInfo getFileById(String id) {
        return fileRepository.findById(id).orElseThrow(() ->
                new RuntimeException("File not found with id: " + id));
    }

    public PageResult<FileInfo> listFiles(String userId, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);

        Page<FileInfo> pages = fileRepository.searchFileInfoByUploadFileUserId(userId, pageRequest);
        PageResult<FileInfo> pageResult = new PageResult<>();
        pageResult.setTotal(pages.getTotalElements());
        pageResult.setPage(pages.getNumber() + 1);
        pageResult.setSize(pages.getSize());
        pageResult.setList(pages.getContent());

        return pageResult;
    }
} 