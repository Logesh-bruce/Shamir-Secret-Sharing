package m1;

import java.math.BigInteger;                   // for very large numbers
import java.nio.file.Files;                    // for reading file
import java.nio.file.Paths;                    // for file path
import java.util.ArrayList;                    // for dynamic list
import java.util.Iterator;                     // for looping through json keys
import java.util.List;                         // for List type
import org.json.JSONObject;                    // for reading json file

public class ShamirSecret{


    static class Point {
        BigInteger x;                          // x value
        BigInteger y;                          // y value


        Point(BigInteger x, BigInteger y) {
            this.x = x;                        // store x
            this.y = y;                        // store y
        }
    }


    public static void main(String[] args) throws Exception {

        // solve testcase1.json and print secret
        System.out.println("Secret 1: " + solve("testcase1.json"));

        // solve testcase2.json and print secret
        System.out.println("Secret 2: " + solve("testcase2.json"));
    }

    // solve method - reads json file and finds the secret
    public static BigInteger solve(String filePath) throws Exception {

        // read entire json file and store as string
        String content = new String(Files.readAllBytes(Paths.get(filePath)));

        // convert string into JSONObject so we can read values from it
        JSONObject json = new JSONObject(content);

        // get the "keys" section from json which has n and k values
        JSONObject keys = json.getJSONObject("keys");

        // get k value - minimum number of points needed to find secret
        int k = keys.getInt("k");

        // create empty list to store all points
        List<Point> points = new ArrayList<>();

        // create iterator to loop through all keys in json one by one
        Iterator<String> iterator = json.keys();

        // keep looping until no more keys left
        while (iterator.hasNext()) {

            // get next key - like "keys", "1", "2", "3"
            String key = iterator.next();

            // if key is "keys" then skip it - we only want numbered keys
            if (key.equals("keys")) continue;

            // get the object for this key - like { "base":"10", "value":"4" }
            JSONObject obj = json.getJSONObject(key);

            // get base value as integer - like 2, 8, 10, 16
            int base = Integer.parseInt(obj.getString("base"));

            // get value string - like "111", "4", "12"
            String value = obj.getString("value");

            // x is the key number converted to BigInteger - like 1, 2, 3
            BigInteger x = new BigInteger(key);

            // y is value converted from given base to base 10
            // example: "111" in base 2 becomes 7 in base 10
            BigInteger y = new BigInteger(value, base);

            // create point with x and y and add to list
            points.add(new Point(x, y));
        }

        // sort all points by x value - smallest x comes first
        points.sort((a, b) -> a.x.compareTo(b.x));

        // take only first k points from sorted list
        List<Point> selected = points.subList(0, k);

        // send k points to lagrange method and return the secret
        return lagrangeAtZero(selected);
    }

    // lagrange method - uses math formula to find f(0) which is the secret
    public static BigInteger lagrangeAtZero(List<Point> points) {

        // result starts at 0 - we keep adding terms to this
        BigInteger result = BigInteger.ZERO;

        // total number of points
        int k = points.size();

        // outer loop - go through each point one by one
        for (int i = 0; i < k; i++) {

            // get x of current point
            BigInteger xi = points.get(i).x;

            // get y of current point
            BigInteger yi = points.get(i).y;

            // numerator starts at 1 - we multiply values into this
            BigInteger numerator = BigInteger.ONE;

            // denominator starts at 1 - we multiply values into this
            BigInteger denominator = BigInteger.ONE;

            // inner loop - go through all other points except current i
            for (int j = 0; j < k; j++) {

                // skip when i equals j - we dont use same point twice
                if (i == j) continue;

                // get x of other point
                BigInteger xj = points.get(j).x;

                // multiply numerator by (-xj)
                // because we find f(0) so x=0, so (0-xj) = -xj
                numerator = numerator.multiply(xj.negate());

                // multiply denominator by (xi - xj)
                denominator = denominator.multiply(xi.subtract(xj));
            }

            // calculate term = yi * numerator / denominator
            BigInteger term = yi.multiply(numerator).divide(denominator);

            // add this term to total result
            result = result.add(term);
        }


        return result;
    }
}
