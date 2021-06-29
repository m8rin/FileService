package com.example.demo.word;

import org.apache.poi.xwpf.usermodel.*;

import java.util.ArrayList;
import java.util.List;

public class WordEditing {

    public static void paragraphsSearch(XWPFDocument docxFile, ArrayList<String> variableList, ArrayList<String> list, String purpose) {
        List<XWPFParagraph> paragraphs = docxFile.getParagraphs();
        for (XWPFParagraph p : paragraphs) {
            List<XWPFRun> runs = p.getRuns();
            if (runs != null) {
                for (XWPFRun r : runs) {
                    String text = r.getText(0);

                    if(purpose.equals("replace")){
                        replaceText(variableList, list, r, text);
                    }else if(purpose.equals("fill")){
                        fillingVariableList(variableList, text);
                    }
                }
            }
        }
    }

    public static void tableSearch(XWPFDocument docxFile, ArrayList<String> variableList, ArrayList<String> list, String purpose) {
        for (XWPFTable tbl : docxFile.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (int i = 0; i < p.getRuns().size(); i++) {
                            XWPFRun r = p.getRuns().get(i);
                            String text = r.getText(0);
                            //System.out.println(text);

                            if(purpose.equals("replace")){
                                replaceText(variableList, list, r, text);
                            }else if(purpose.equals("fill")){
                                fillingVariableList(variableList, text);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void replaceText(ArrayList<String> variableList, ArrayList<String> list, XWPFRun r, String text) {
        for (int i = 0; i < variableList.size(); i++) {
            if (text != null && text.contains(variableList.get(i))) {
                text = text.replace(variableList.get(i), list.get(i));
                System.out.println(variableList.get(i) + " = " + text);
                r.setText(text, 0);
            }
        }
    }

    public static void fillingVariableList(ArrayList<String> list, String text) {
        if (text != null) {
            char str_char = text.charAt(0);
            char variable_symbol = '{';
            if(str_char == variable_symbol){
                list.add(text);
                //System.out.println(text);
            }
        }
    }

    public static void fillingList(ArrayList<String> list) {
        list.add("ООО 'Круто1'");
        list.add("г. Уфа, ул. Кольцевая, 71");
        list.add("89666665521");
        list.add("778991");
        list.add("220011");
        list.add("15.06.2021");
        list.add("12341");
        list.add("Такой-то1");
        list.add("Иванов Иван Иванович1");
        list.add("г. Уфа, ул. Кольцевая, 72");
        list.add("+7 967 74 77 777");
    }
}
