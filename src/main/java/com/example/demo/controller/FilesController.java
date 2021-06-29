package com.example.demo.controller;

import com.example.demo.model.FileEntity;
import com.example.demo.model.FileResponse;
import com.example.demo.service.FileService;
import com.example.demo.word.WordEditing;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FilesController {

    @GetMapping("/showTemplates")
    public String findTemplates(Model model) {

        var files = (List<FileResponse>) fileService.getFile("1")
                .stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());


        List<String> fileNames = new ArrayList<>();
        for (FileResponse file : files) {
            fileNames.add(file.getName());
        }

        model.addAttribute("files", fileNames);

        return "showTemplates";
    }

    private final FileService fileService;

    @Autowired
    public FilesController(FileService fileService) {
        this.fileService = fileService;
    }

    @RequestMapping(value = "/files", method = RequestMethod.POST)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            fileService.save(file);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(String.format("Файл успешно загружен: %s", file.getOriginalFilename()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Не удалось загрузить файл: %s!", file.getOriginalFilename()));
        }
    }

    @RequestMapping(value = "/files", method = RequestMethod.GET)
    public List<FileResponse> list() {

        return fileService.getAllFiles()
                .stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }


    private FileResponse mapToFileResponse(FileEntity fileEntity) {
        String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(fileEntity.getId())
                .toUriString();
        FileResponse fileResponse = new FileResponse();
        fileResponse.setId(fileEntity.getId());
        fileResponse.setName(fileEntity.getName());
        fileResponse.setContentType(fileEntity.getContentType());
        fileResponse.setSize(fileEntity.getSize());
        fileResponse.setUrl(downloadURL);

        return fileResponse;
    }

    @RequestMapping(value = "/files/{id}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

        if (fileEntityOptional.isEmpty()) {
            return ResponseEntity.notFound()
                    .build();
        }

        FileEntity fileEntity = fileEntityOptional.get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName() + "\"")
                .contentType(MediaType.valueOf(fileEntity.getContentType()))
                .body(fileEntity.getData());
    }

    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public Map<String, String> uploadFile(@RequestParam(value = "file") MultipartFile file) {
        String textFileName = file.getOriginalFilename();

        // Создаем объект мапа для хранения содержимого
        Map<String, String> wordMap = new LinkedHashMap<>();
        try {
            assert textFileName != null;
            if (textFileName.endsWith(".docx")) {

                // Создаем временный файл
                String tempFile = "tempFile.docx";
                File uFile = new File(tempFile);

                // Копируем содержимое файла
                FileCopyUtils.copy(file.getBytes(), uFile);

                OPCPackage opcPackage = POIXMLDocument.openPackage(tempFile);
                // Использование класса XWPFDocument компонента XWPF для получения содержимого документа
                XWPFDocument document = new XWPFDocument(opcPackage);

                ArrayList<String> variableList = new ArrayList<>();
                ArrayList<String> list = new ArrayList<>();

                //заполнение списка переменных
                WordEditing.tableSearch(document, variableList, list, "fill");
                WordEditing.paragraphsSearch(document, variableList, list, "fill");

                //заполнение списка значений, на которые нужно поменять переменные
                WordEditing.fillingList(list);

                //поиск переменных в таблицах и замена
                WordEditing.tableSearch(document, variableList, list, "replace");
                //поиск переменных в абзацах и замена
                WordEditing.paragraphsSearch(document, variableList, list, "replace");

                List<XWPFParagraph> paras = document.getParagraphs();
                int i = 1;
                for (XWPFParagraph paragraph : paras) {
                    String words = paragraph.getText();
                    //System.out.println(words);
                    wordMap.put("(" + i + ")", words);
                    i++;
                }

                //записываем в новый файл
                String outputFileName = "Output.docx";
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(outputFileName);

                    document.write(fileOutputStream);

                    //преобразуем в multipartFile для сохранения
                    Path path = Paths.get(outputFileName);
                    String name = "file.docx";
                    String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    byte[] content = null;
                    try {
                        content = Files.readAllBytes(path);
                    } catch (final IOException ignored) {
                    }
                    MultipartFile result = new MockMultipartFile(name,
                            outputFileName, contentType, content);

                    fileService.save(result);

                } catch (IOException ignored) {
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        document.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                uFile.delete();
                System.out.println("\nНовый файл успешно сохранен!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(wordMap);
        return wordMap;
    }
}