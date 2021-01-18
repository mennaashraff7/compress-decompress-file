package huff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Map.Entry;


class HuffmanNode {

    int freq;
    Character c;
    HuffmanNode left;
    HuffmanNode right;

    public HuffmanNode(Character c, Integer freq, HuffmanNode left, HuffmanNode right) {
        this.left = left;
        this.right = right;
        this.c = c;
        this.freq = freq;
    }

    HuffmanNode(Character c, Integer freq) {
        this.c = c;
        this.freq = freq;
    }

}

class MyComparator implements Comparator<HuffmanNode> {

    public int compare(HuffmanNode x, HuffmanNode y) {
        return (x.freq - y.freq);
    }
}

public class huffman {
    public static int padding = 0;
    public static StringBuilder sb = new StringBuilder();
    public static StringBuilder bin_trimmed = new StringBuilder(); // binary trimmed
    public static StringBuilder tempString = new StringBuilder();
    public static StringBuilder decodedString = new StringBuilder();
    public static boolean flag = true; // true is header
    public static Map<Character, String> encodedMap = new HashMap<>(); // each character and huffman code
    public static HashMap<Character, Integer> char_freq = new HashMap<Character, Integer>(); // character and its
                                                                                             // frequency
    public static HashMap<Character, Integer> char_freq2 = new HashMap<Character, Integer>();
    public static HashMap<Character, String> decodedMap = new HashMap<Character, String>();
    public static boolean spaces = false;

    public static void main(String[] args) throws IOException {

        System.out.println("CHOOSE 1 OR 2:\n" + "1.COMPRESS\n" + "2.DECOPMRESS\n");
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        switch (n) {
            case 1: {
                try {
                    String filepath = "";
                    Scanner scanner_c = new Scanner(System.in); // scanner for command line
                    System.out.println("Enter the file path to be compressed: ");  
                    filepath = scanner_c.nextLine();             //taking your file path to be compressed
                    File inFile = new File(filepath.toString());
                    Scanner myReader = new Scanner(inFile);

                    String data = myReader.useDelimiter("\\A").next();  //reading the file
                    if (data.contains("\r"))
                        data = data.replaceAll("\r", "~");
                    if (data.contains("\n"))
                        data = data.replaceAll("\n", "+");

                    myReader.close();

                    long starttime = System.nanoTime();    //the execution time of encoding
                    buildHuffmanTree(data);                // encoding and compressing the file
                    long finishtime = System.nanoTime();

                    long time = finishtime - starttime;
                    System.out.println("Time in nanosecond:   " + time);

                    File out = writeCompressedFile();    // writing the compressed file 
                    double compression_ratio = (1.0 * (out.length()) / (inFile.length())) * 100;  //calculating the compression ratio
                    System.out.println("Compression ratio is: " + compression_ratio);

                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
                print(); // to print the ascii code for each character and its huffman code
            }
                break;
            /////////////////////////////////////// DECOMPRESSING FILES ///////////////////////////////////////////////////////
            case 2: {
                StringBuilder body = new StringBuilder();
                String data2 = "";
                int p2 = 0;
                try {

                    String filepath = "";
                    Scanner scanner_c = new Scanner(System.in); // scanner for command line
                    System.out.println("Enter the file path to decompress: "); 
                    filepath = scanner_c.nextLine(); // reading the file path to decompress

                    File myObj = new File(filepath);

                    Scanner myReader2 = new Scanner(myObj).useDelimiter("\\n");
                    while (myReader2.hasNext()) {                     //reading the compressed file
                        data2 = myReader2.next();
                        if (data2.equalsIgnoreCase("header")) {
                            flag = true;
                            continue;
                        } else if (data2.equalsIgnoreCase("body")) {
                            flag = false;
                            continue;
                        }
                        if (flag) {
      
                            String[] m = data2.split(">");              // adding the character, huffman code and its freq in maps
                                                                        //  while reading the file
                            if (m.length == 3 && !m[0].equalsIgnoreCase("padding")) {
                                for (char c : m[0].toCharArray()) {
                                    decodedMap.put(c, m[1]);
                                    char_freq2.put(c, Integer.parseInt(m[2]));
                                }
                            } else if (m.length == 2) {
                                if (m[0].equalsIgnoreCase("padding")) {
                                    p2 = Integer.parseInt(m[1]);
                                }
                            }

                        } else {          //reading the compressed body

                            body.append(data2);

                        }

                    }
                    myReader2.close();

                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }

                long starttime = System.nanoTime();          //calculate the executing time of decompression
                StringBuilder bin = AsciiToBinary(body, 8);  // converting the body(ascii code (compressed)) to binary 

                String s = bin.substring(0, bin.length() - p2);  //removing the padding
                bin_trimmed.append(s);

                decode(decodedMap);   // decoding the compressed file
                long finishtime = System.nanoTime();
                long time = finishtime - starttime;
                System.out.println("Decompression time in nanosecond: " + time);

            }
                break;
        }

    }

    public static void buildHuffmanTree(String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        for (char c : text.toCharArray()) {   //putting the character and its frequency in the map
            char_freq.put(c, char_freq.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<HuffmanNode> pq;     // make a priority queue to store nodes of the Huffman tree
        pq = new PriorityQueue<>(Comparator.comparingInt(l -> l.freq));

        for (Entry<Character, Integer> entry : char_freq.entrySet()) {    //a leaf node for each character and add it to the priority queue.
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (pq.size() != 1) {      // Until there is more than one node in the queue

            HuffmanNode left = pq.poll();    // Remove the two nodes of the highest priority
            HuffmanNode right = pq.poll();

            pq.add(new HuffmanNode(null, (left.freq + right.freq), left, right));     // Create a new internal node with these two nodes as children
                                                                                        // and with a frequency equal to the sum of the two nodes'
                                                                                        // frequencies. Add the new node to the priority queue.
        }

        HuffmanNode root = pq.peek();   

        encode(root, "", encodedMap);  //traverse the huffman tree

        for (char c : text.toCharArray()) {
            sb.append(encodedMap.get(c));
        }

        int code_l = sb.length();

        if (code_l % 8 != 0) {           //for padding if length not divisible by 8 then adding zeros to be divisible by 8
            int p = 8 - (code_l % 8);
            padding = p;
            code_l += padding;
            for (int x = 0; x < padding; x++) {
                sb.append('0');
            }
        }
    }

    public static void decode(HashMap<Character, String> map) throws IOException {

        for (int i = 0; i < bin_trimmed.length(); i++) {     // taking each character from the bintrimmed until 
            tempString.append(bin_trimmed.charAt(i));        // it is found in the map of the huffman code 
            for (Entry<Character, String> entry : map.entrySet()) {
                if (entry.getValue().contentEquals(tempString)) {     //when it is found we print the character
                    if (entry.getKey().equals('~')) {
                        decodedString.append("\r");
                        tempString.delete(0, tempString.length());
                        tempString.append("");
                    } 
                    else if (entry.getKey().equals('+')) {
                        decodedString.append("\n");
                        tempString.delete(0, tempString.length());
                        tempString.append("");
                    } 
                    else {
                        decodedString.append(entry.getKey());
                        tempString.delete(0, tempString.length());
                        tempString.append("");
                    }
                }
            }
        }
        String filepath = "";
        Scanner scanner_c = new Scanner(System.in); // scanner for command line
        System.out.println("Enter the file path to decompress in:");   
        filepath = scanner_c.nextLine();
        FileWriter myWriter = new FileWriter(filepath);

        myWriter.write(decodedString.toString());   //writng the decompressed file

        myWriter.close();
    }

    public static void encode(HuffmanNode root, String str, Map<Character, String> huffmanCode) {
        if (root == null) {
            return;
        }

        if (isLeaf(root)) {
            huffmanCode.put(root.c, str.length() > 0 ? str : "1");
        }

        encode(root.left, str + '0', huffmanCode);
        encode(root.right, str + '1', huffmanCode);
    }

    public static boolean isLeaf(HuffmanNode root) {
        return root.left == null && root.right == null;
    }

    public static StringBuilder AsciiToBinary(StringBuilder str, int bits) {

        StringBuilder result = new StringBuilder();
        String tmpStr;
        int tmpInt;
        String h = str.toString();
        char[] messChar = h.toCharArray();

        for (int i = 0; i < messChar.length; i++) {
            tmpStr = Integer.toBinaryString(messChar[i]);
            tmpInt = tmpStr.length();
            if (tmpInt != bits) {
                tmpInt = bits - tmpInt;
                if (tmpInt == bits) {
                    result.append(tmpStr);
                } else if (tmpInt > 0) {
                    for (int j = 0; j < tmpInt; j++) {
                        result.append('0');
                    }
                    result.append(tmpStr);
                } else {
                    System.err.println("argument 'bits' is too small");
                }
            } else {
                result.append(tmpStr);
            }

        }

        return result;
    }

    public static void print() {
        System.out.println("Byte" + "\t\t\t" + "Code" + "\t\t\t" + "NewCode");

        for (Entry<Character, String> entry2 : encodedMap.entrySet()) {
            {
                char c = entry2.getKey();
                int s = (int) c;

                System.out.println(s + "\t\t\t" + Integer.toBinaryString(s) + "\t\t\t" + entry2.getValue());

            }

        }
    }

    public static File writeCompressedFile() {

        String filepath = "";
        Scanner scanner_c = new Scanner(System.in); // scanner for command line
        System.out.println("Enter the file path to compress in:");
        filepath = scanner_c.nextLine();
        File outFile = new File(filepath);
        try {                                                 // writing the compressed file by a header format then the padding and the body

            FileWriter myWriter = new FileWriter(outFile);
            myWriter.write("Header\n");
            for (Entry<Character, String> entry1 : encodedMap.entrySet()) {

                myWriter.write(entry1.getKey() + ">" + entry1.getValue() + ">" + char_freq.get(entry1.getKey()) + "\n");

            }
            myWriter.write("padding" + ">" + padding + "\n");
            StringBuilder s2 = new StringBuilder();
            char ASCII;
            myWriter.write("body\n");
            for (int i = 0; i <= sb.length() - 8; i += 8) {

                ASCII = (char) Integer.parseInt(sb.substring(i, i + 8), 2);
                s2.append(ASCII);
            }
            myWriter.write(s2.toString());
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();

        }
        return outFile;
    }

}
