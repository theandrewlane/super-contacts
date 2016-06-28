package com.k1ngdr3w.supercontacts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class DriversLicense {

    private String dataString;
    private HashMap<String, String> dataHash;
    private Decoder decoder;

    public DriversLicense(String barCode) {
        dataString = barCode;
        decoder = new Decoder(barCode);
        dataHash = decoder.getSubFile();
    }

    public String dlFormat(String str) {
        str = str.toLowerCase();
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            ret.append(Character.toUpperCase(words[i].charAt(0)));
            ret.append(words[i].substring(1));
            if (i < words.length - 1) {
                ret.append(' ');
            }
        }
        return ret.toString();
    }


    public Decoder getDecoder() {
        return decoder;
    }

    /**
     * Get extracted first name or parse from name string.
     *
     * @return Examples:
     * <p/>
     * Name: "TianYu Huang", firstName: "TianYu";
     * <p/>
     * Name: "Tian Yu Huang", firstName: "Tian";
     * <p/>
     * Name: "Tian Yu Z Huang", firstName: "Tian Yu Z";
     */
    public String getFirstName() {
        String firstName = dataHash.get("FirstName");
        if (firstName != null && !firstName.isEmpty()) {
            firstName = firstName.trim();

        } else {
            String name = dataHash.get("Name");
            if (name != null && !name.isEmpty()) {
                String[] nameTokens = name.split(" ");
                if (nameTokens.length <= 3) {
                    firstName = nameTokens[0].trim();
                } else {
                    for (int i = 1; i < nameTokens.length; i++) {
                        firstName += nameTokens[i].trim();
                        if (i < nameTokens.length - 1) {
                            firstName += " ";
                        }
                    }
                }
            } else {
                firstName = "";
            }
        }
        return dlFormat(firstName);
    }

    /**
     * Get extracted last name or parse from name string.
     *
     * @return Examples:
     * <p/>
     * Name: "TianYu Huang", lastName: "Huang";
     * <p/>
     * Name: "Tian Yu Huang", lastName: "Huang";
     * <p/>
     * Name: "Tian Yu Z Huang", lastName: "Huang";
     */
    public String getLastName() {
        String lastName = dataHash.get("LastName");
        if (lastName != null && !lastName.isEmpty()) {
            lastName = lastName.trim();
        } else {
            String name = dataHash.get("Name");
            if (name != null && !name.isEmpty()) {
                String[] nameTokens = name.split(" ");
                if (nameTokens.length == 1) {
                    lastName = "";
                } else {
                    lastName = nameTokens[nameTokens.length - 1].trim();
                }
            } else {
                lastName = "";
            }
        }
        return dlFormat(lastName);
    }

    /**
     * Get extracted state
     *
     * @return 2-Letter state abbreviations
     */
    public String getState() {
        String state = dataHash.get("State");
        if (state != null && !state.isEmpty()) {
            state = state.trim().toUpperCase();
        } else {
            state = "";
        }
        return state;
    }

    /**
     * Get extracted address
     *
     * @return Address
     */
    public String getAddress() {
        String address = dataHash.get("Address");
        if (address != null && !address.isEmpty()) {
            address = address.trim();
        } else {
            address = "";
        }
        return dlFormat(address);
    }

    /**
     * Get extracted city
     *
     * @return City
     */
    public String getCity() {
        String city = dataHash.get("City");
        if (city != null && !city.isEmpty()) {
            city = city.trim();
        } else {
            city = "";
        }
        return dlFormat(city);
    }

    /**
     * Get extracted ZIP code
     *
     * @return ZIP code
     */
    public String getZipCode() {
        String zipCode = dataHash.get("ZipCode");
        if (zipCode != null && !zipCode.isEmpty()) {
            zipCode = zipCode.trim();
        } else {
            zipCode = "";
        }
        return zipCode;
    }

    /**
     * Get extracted country
     *
     * @return Country
     */
    public String getCountry() {
        String country = dataHash.get("Country");
        if (country != null && !country.isEmpty()) {
            country = country.trim().toUpperCase();
        } else {
            country = "";
        }
        return country;
    }


    /**
     * Get parsed DOB
     *
     * @return DOB
     */
    public Calendar getDOBCal() {
        Calendar calendar = null;

        String dob = dataHash.get("DOB");
        if (dob != null && !dob.isEmpty()) {
            calendar = parseDate(dob);

        } else {
            // Not found
        }
        return calendar;
    }

    /**
     * Get parsed DOB
     *
     * @return DOB
     */
    public String getDOB() {

        return formatDate(getDOBCal());
    }


    /**
     * Get parsed Height
     *
     * @return Height
     */
    public float getHeight() {
        String height = dataHash.get("Height");
        if (height != null && !height.isEmpty()) {
            height = height.trim().replaceAll("[\\D]", "");
        } else {
            height = "";
        }
        return Float.parseFloat(height);
    }

    /**
     * Get current object representation in JSON string
     *
     * @return Serialized string in JSON
     */
    public String toJson() {
        String json = "";
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();

        HashMap jsonHash = new HashMap();
        jsonHash.put("first_name", getFirstName());
        jsonHash.put("last_name", getLastName());
        jsonHash.put("address", getAddress());
        jsonHash.put("city", getCity());
        jsonHash.put("state", getState());
        jsonHash.put("zipcode", getZipCode());
        jsonHash.put("height", getHeight());
        jsonHash.put("dob", formatDate(getDOBCal()));
        json = gson.toJson(jsonHash);
        return json;
    }

    // -----------------------------------------------------------------------//

    protected Calendar parseDate(String date) {
        String format = "ISO";
        int potentialYear = Integer.parseInt(date.substring(0, 4));
        if (potentialYear > 1300) {
            format = "Other";
        }

        // Parse calendar based on format
        int year, month, day;
        Calendar calendar = Calendar.getInstance();
        if (format.equals("ISO")) {
            year = Integer.parseInt(date.substring(4, 8));
            month = Integer.parseInt(date.substring(0, 2));
            day = Integer.parseInt(date.substring(2, 4));
        } else {
            year = Integer.parseInt(date.substring(0, 4));
            month = Integer.parseInt(date.substring(4, 6));
            day = Integer.parseInt(date.substring(6, 8));
        }
        calendar.set(year, month - 1, day, 0, 0, 0);
        return calendar;
    }

    protected String formatDate(Calendar date) {
        return new SimpleDateFormat("MM/dd/yyyy").format(date.getTime());
    }
}