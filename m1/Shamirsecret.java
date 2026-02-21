package m1;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

public class ShamirSecret {

    // Prime field (small demo prime — can upgrade later)
    private static final BigInteger PRIME =
            new BigInteger("104729");

    static class Point {
        BigInteger x;
        BigInteger y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // 🔥 Runs BOTH testcases automatically
    public static void main(String[] args) throws Exception {

        System.out.println("Recovered Secret 1: " + solve("testcase1.json"));
        System.out.println("Recovered Secret 2: " + solve("testcase2.json"));
    }

    public static BigInteger solve(String filePath) throws Exception {

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject json = new JSONObject(content);

        JSONObject keys = json.getJSONObject("keys");
        int k = keys.getInt("k");

        List<Point> points = new ArrayList<>();
        Iterator<String> iterator = json.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();

            if (key.equals("keys")) continue;

            JSONObject obj = json.getJSONObject(key);

            int base = Integer.parseInt(obj.getString("base"));
            String value = obj.getString("value");

            BigInteger x = new BigInteger(key);
            BigInteger y = new BigInteger(value, base).mod(PRIME);

            points.add(new Point(x, y));
        }

        points.sort((a, b) -> a.x.compareTo(b.x));
        List<Point> selected = points.subList(0, k);

        return lagrangeAtZero(selected);
    }

    public static BigInteger lagrangeAtZero(List<Point> points) {

        BigInteger result = BigInteger.ZERO;
        int k = points.size();

        for (int i = 0; i < k; i++) {

            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {

                if (i == j) continue;

                BigInteger xj = points.get(j).x;

                numerator = numerator.multiply(xj.negate()).mod(PRIME);
                denominator = denominator.multiply(xi.subtract(xj)).mod(PRIME);
            }

            BigInteger inverse = denominator.modInverse(PRIME);

            BigInteger term = yi.multiply(numerator)
                    .mod(PRIME)
                    .multiply(inverse)
                    .mod(PRIME);

            result = result.add(term).mod(PRIME);
        }

        return result.mod(PRIME);
    }
}
