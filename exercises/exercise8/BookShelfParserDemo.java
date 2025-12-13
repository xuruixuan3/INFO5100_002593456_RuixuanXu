import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BookShelfParserDemo {

    // ---------- JSON Model ----------
    static class BookShelf {
        List<Book> books = new ArrayList<>();
    }

    static class Book {
        String title;
        int publishedYear;
        int numberOfPages;
        List<String> authors = new ArrayList<>();

        Book() {}
        Book(String title, int publishedYear, int numberOfPages, List<String> authors) {
            this.title = title;
            this.publishedYear = publishedYear;
            this.numberOfPages = numberOfPages;
            this.authors = authors;
        }
    }

    private static final String XML_FILE = "books.xml";
    private static final String JSON_FILE = "books.json";

    public static void main(String[] args) throws Exception {

        System.out.println("===== Exercise 8: Parse XML & JSON + Add Book Programmatically =====\n");

        // 1) Parse original XML
        System.out.println("----- XML PARSER OUTPUT (Original) -----");
        List<Book> xmlBooks = parseBooksFromXml(XML_FILE);
        printBooks(xmlBooks);

        // 2) Parse original JSON
        System.out.println("\n----- JSON PARSER OUTPUT (Original) -----");
        BookShelf jsonShelf = parseBooksFromJson(JSON_FILE);
        printBooks(jsonShelf.books);

        // 3) Add new book programmatically (to BOTH XML and JSON), then write back
        Book newBook = new Book(
                "Clean Code",
                2008,
                464,
                Arrays.asList("Robert C. Martin")
        );

        System.out.println("\n===== ADDING NEW BOOK PROGRAMMATICALLY =====");
        System.out.println("Adding: " + newBook.title);

        addBookToXml(XML_FILE, newBook);
        addBookToJson(JSON_FILE, newBook);

        // 4) Re-parse and print again to confirm
        System.out.println("\n----- XML PARSER OUTPUT (Updated) -----");
        List<Book> xmlBooksUpdated = parseBooksFromXml(XML_FILE);
        printBooks(xmlBooksUpdated);

        System.out.println("\n----- JSON PARSER OUTPUT (Updated) -----");
        BookShelf jsonShelfUpdated = parseBooksFromJson(JSON_FILE);
        printBooks(jsonShelfUpdated.books);

        System.out.println("\nDone. Updated files written: " + XML_FILE + ", " + JSON_FILE);
    }

    // ========================= XML =========================

    private static List<Book> parseBooksFromXml(String xmlPath) throws Exception {
        Document doc = loadXml(xmlPath);
        doc.getDocumentElement().normalize();

        NodeList bookNodes = doc.getElementsByTagName("Book");
        List<Book> books = new ArrayList<>();

        for (int i = 0; i < bookNodes.getLength(); i++) {
            Node node = bookNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element bookEl = (Element) node;

            Book b = new Book();
            b.title = getText(bookEl, "title");
            b.publishedYear = Integer.parseInt(getText(bookEl, "publishedYear"));
            b.numberOfPages = Integer.parseInt(getText(bookEl, "numberOfPages"));

            b.authors = new ArrayList<>();
            Element authorsEl = (Element) bookEl.getElementsByTagName("authors").item(0);
            if (authorsEl != null) {
                NodeList authorNodes = authorsEl.getElementsByTagName("author");
                for (int a = 0; a < authorNodes.getLength(); a++) {
                    b.authors.add(authorNodes.item(a).getTextContent().trim());
                }
            }
            books.add(b);
        }
        return books;
    }

    private static void addBookToXml(String xmlPath, Book newBook) throws Exception {
        Document doc = loadXml(xmlPath);
        Element root = doc.getDocumentElement(); // BookShelf

        Element bookEl = doc.createElement("Book");

        Element titleEl = doc.createElement("title");
        titleEl.appendChild(doc.createTextNode(newBook.title));
        bookEl.appendChild(titleEl);

        Element yearEl = doc.createElement("publishedYear");
        yearEl.appendChild(doc.createTextNode(String.valueOf(newBook.publishedYear)));
        bookEl.appendChild(yearEl);

        Element pagesEl = doc.createElement("numberOfPages");
        pagesEl.appendChild(doc.createTextNode(String.valueOf(newBook.numberOfPages)));
        bookEl.appendChild(pagesEl);

        Element authorsEl = doc.createElement("authors");
        for (String author : newBook.authors) {
            Element authorEl = doc.createElement("author");
            authorEl.appendChild(doc.createTextNode(author));
            authorsEl.appendChild(authorEl);
        }
        bookEl.appendChild(authorsEl);

        root.appendChild(bookEl);
        saveXml(doc, xmlPath);
    }

    private static Document loadXml(String xmlPath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new File(xmlPath));
    }

    private static void saveXml(Document doc, String xmlPath) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), new StreamResult(new File(xmlPath)));
    }

    private static String getText(Element parent, String tag) {
        Node n = parent.getElementsByTagName(tag).item(0);
        return (n == null) ? "" : n.getTextContent().trim();
    }

    // ========================= JSON =========================

    private static BookShelf parseBooksFromJson(String jsonPath) throws Exception {
        String json = Files.readString(Path.of(jsonPath), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        return gson.fromJson(json, BookShelf.class);
    }

    private static void addBookToJson(String jsonPath, Book newBook) throws Exception {
        BookShelf shelf = parseBooksFromJson(jsonPath);
        shelf.books.add(newBook);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String updated = gson.toJson(shelf);
        Files.writeString(Path.of(jsonPath), updated + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    // ========================= Print =========================

    private static void printBooks(List<Book> books) {
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            System.out.println("Book #" + (i + 1));
            System.out.println("  Title         : " + b.title);
            System.out.println("  Published Year: " + b.publishedYear);
            System.out.println("  Pages         : " + b.numberOfPages);
            System.out.println("  Authors       : " + String.join(", ", b.authors));
        }
    }
}
