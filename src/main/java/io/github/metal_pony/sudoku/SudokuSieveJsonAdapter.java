package io.github.metal_pony.sudoku;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * SudokuSieveJsonAdapter is a JSON TypeAdapter to be registered with Gson
 * for handling SudokuSieve objects.
 *
 * A SudokuSieve JSON object looks like the following:
 * <code>
 * {
 *   "config": string,
 *   "items": string[]
 * }
 * </code>
 * where `config` is a valid Sudoku configuration, and `items` are BigIntegers
 * as strings.
 */
public class SudokuSieveJsonAdapter extends TypeAdapter<SudokuSieve> {
    /** Error message displayed when encountering unexpected json.*/
    static final String UNEXPECTED_NAME = "Unexpected name in sudoku sieve json: %s";

    /** Error message displayed when encountering unexpected json.*/
    static final String ITEM_NOT_ADDED = "Sieve item was not added: %s";

    /**
     * Creates a new SudokuSieveJsonAdapter.
     */
    public SudokuSieveJsonAdapter() {
        super();
    }

    @Override
    public void write(JsonWriter writer, SudokuSieve sieve) throws IOException {
        writer.beginObject();
        writer.name("config");
        writer.value(sieve.config().toString());
        writer.name("items");
        writer.beginArray();
        for (SudokuMask item : sieve.items(new ArrayList<>())) {
            writer.value(item.toBigInt().toString());
        }
        writer.endArray();
        writer.endObject();
    }

    @Override
    public SudokuSieve read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        Sudoku config = null;
        List<SudokuMask> items = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "config":
                    config = new Sudoku(reader.nextString());
                    break;
                case "items":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        String itemStr = reader.nextString();
                        SudokuMask item = new SudokuMask(new BigInteger(itemStr));
                        items.add(item);
                    }
                    reader.endArray();
                    break;
                default:
                    throw new JsonParseException(String.format(UNEXPECTED_NAME, name));
            }
        }
        reader.endObject();

        SudokuSieve sieve = new SudokuSieve(config);
        items.forEach(item -> {
            if (!sieve.add(item)) {
                throw new JsonParseException(
                    String.format(ITEM_NOT_ADDED, item.toBigInt().toString())
                );
            }
        });

        return sieve;
    }
}
