package com.example.demo.controller;

import com.example.demo.model.FileEntity;
import com.example.demo.model.FileResponse;
import com.example.demo.model.Variable;
import com.example.demo.poi.word.WordEditing;
import com.example.demo.service.FileService;
import com.example.demo.service.interfaces.IVariableService;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
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

@Controller
public class FilesController {

    @GetMapping("/showTemplates")
    public String findTemplates(Model model) {

        var templates = (List<FileEntity>) fileService.getAllFiles();

        model.addAttribute("files", templates);

        return "showTemplates";
    }

    private final FileService fileService;

    private final IVariableService iVariableService;

    @Autowired
    public FilesController(FileService fileService, @Qualifier("IVariableService") IVariableService iVariableService) {
        this.fileService = fileService;
        this.iVariableService = iVariableService;
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

    @ResponseBody
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

    @ResponseBody
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

    // получение переменных из шаблона
    @ResponseBody
    @RequestMapping(value = "/getVariable", method = RequestMethod.POST)
    public Map<String, String> getVariable(@RequestParam("file") MultipartFile file) {
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

                //сохранение переменных и определение их типа
                VariableController variableController = new VariableController(iVariableService);

                char variable_symbol = '{';
                for (int i = 0; i < variableList.size(); i++) {
                    Variable variable = new Variable();
                    variable.setName(variableList.get(i));
                    wordMap.put("(" + i + ")", variableList.get(i));

                    if (variableList.get(i).length() > 1) {
                        char str_char = variableList.get(i).charAt(0);
                        char str_char1 = variableList.get(i).charAt(1);

                        if (str_char == variable_symbol && str_char1 != '(') {
                            variable.setType("string");
                        } else if (str_char1 == '(') {
                            char str_char2 = variableList.get(i).charAt(2);
                            if (str_char2 == 'i') {
                                variable.setType("int");
                            }
                            if (str_char2 == 'd') {
                                variable.setType("date");
                            }
                        }
                    }

                    variableController.create(variable);
                }
                fileService.save(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n" + wordMap);
        return wordMap;
    }

    //замена переменных в шаблоне и сохранение файла
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadFile(@RequestParam("variable") ArrayList<String> variables, @RequestParam("id") String id, Model model) {

        FileEntity receivedFile = new FileEntity();
        Optional<FileEntity> optionalFileEntity = fileService.getFile(id);
        FileEntity fileEntity = optionalFileEntity.get();

        //String textFileName = file.getOriginalFilename();
        String textFileName = fileEntity.getName();
        try {
            assert textFileName != null;
            if (textFileName.endsWith(".docx")) {

                // Создаем временный файл
                String tempFile = "tempFile1.docx";
                File uFile = new File(tempFile);

                // Копируем содержимое файла
                //FileCopyUtils.copy(file.getBytes(), uFile);
                FileCopyUtils.copy(fileEntity.getData(), uFile);

                OPCPackage opcPackage = POIXMLDocument.openPackage(tempFile);
                // Использование класса XWPFDocument компонента XWPF для получения содержимого документа
                XWPFDocument document = new XWPFDocument(opcPackage);

                ArrayList<String> variableList = new ArrayList<>();

                //заполнение списка переменных
                WordEditing.tableSearch(document, variableList, variables, "fill");
                WordEditing.paragraphsSearch(document, variableList, variables, "fill");

                //поиск переменных в таблицах и замена
                WordEditing.tableSearch(document, variableList, variables, "replace");
                //поиск переменных в абзацах и замена
                WordEditing.paragraphsSearch(document, variableList, variables, "replace");

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
                    byte[] content;
                    try {
                        content = Files.readAllBytes(path);
                        MultipartFile result = new MockMultipartFile(name,
                                outputFileName, contentType, content);

                        fileService.save(result);
                        
                        //получаем данные созданного файла для получения ссылки для скачивания
                        receivedFile = fileService.getLast();

                    } catch (final IOException ignored) {
                    }
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
        model.addAttribute("downloadFiles", mapToFileResponse(receivedFile));
        return "upload";
    }
}