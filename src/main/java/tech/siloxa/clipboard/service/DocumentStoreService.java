package tech.siloxa.clipboard.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.siloxa.clipboard.config.ApplicationProperties;

import javax.annotation.Resource;
import java.io.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class DocumentStoreService {

    @Resource(name = "publicPath")
    private String publicPath;

    @Resource
    private ApplicationProperties applicationProperties;

    public void copyDocument(String from, String to) {
        try (
            InputStream in = new BufferedInputStream(
                new FileInputStream(new ClassPathResource(from).getFile()));
            OutputStream out = new BufferedOutputStream(
                new FileOutputStream(to))) {
            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String storeDocument(final MultipartFile multipartFile) {
        try {
            final String[] imageName = Objects.requireNonNull(multipartFile.getOriginalFilename()).split("\\.");
            final String extension = imageName[imageName.length - 1];
            final String fileName = UUID.randomUUID() + "." + extension;
            final File file = new File(publicPath + fileName);
            final FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
            fileOutputStream.write(multipartFile.getBytes());
            fileOutputStream.close();
            return applicationProperties.getCdn() + fileName;
        } catch (IOException e) {
            return "";
        }
    }

    public boolean removeDocument(String documentName) {
        if (StringUtils.isNotBlank(documentName)) {
            final File file = new File(publicPath + documentName.replaceAll(applicationProperties.getCdn(), ""));
            return file.delete();
        }
        return true;
    }
}
