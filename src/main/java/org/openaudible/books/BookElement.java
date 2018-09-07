package org.openaudible.books;

// Audio Book Attributes.
//
public enum BookElement {
    product_id, codec, asin, infoLink, fullTitle, author, narratedBy, summary, description, duration, format, rating_average, rating_count, release_date, purchase_date, publisher, genre, shortTitle, copyright, user_id, cust_id, order_number, author_link;

    public static BookElement findByName(String s) {

        try {
            return BookElement.valueOf(s);
        } catch (Throwable th) {
            switch (s) {
                case "title":
                    return shortTitle;
                case "DownloadType":
                    break;
                case "domain":
                    break;
                case "awtype":
                    break;
                case "transfer_player":
                    break;

                default:
                    //
                    System.out.println("No BookElement:" + s);
                    break;
            }


        }
        return null;
    }


    public String displayName() {
        String o = this.name();

        switch (this) {

            case product_id:
                o = "Product ID";
                break;
            case codec:
                o = "Codec";
                break;
            case asin:
                o = "ASIN";
                break;
            case infoLink:
                o = "Link";
                break;
            case fullTitle:
                o = "Title";
                break;
            case author:
                o = "Author";
                break;
            case narratedBy:
                o = "Narrated By";
                break;
            case summary:
                o = "Summary";
                break;
            case description:
                o = "Description";
                break;
            case duration:
                o = "Duration";
                break;
            case format:
                o = "Format";
                break;
            case rating_average:
                o = "Ave. Rating";
                break;
            case rating_count:
                o = "Rating Count";
                break;
            case release_date:
                o = "Release Date";
                break;
            case purchase_date:
                o = "Purchase Date";
                break;
            case publisher:
                o = "Publisher";
                break;
            case genre:
                o = "Genre";
                break;
            case shortTitle:
                o = "Title (short)";
                break;
            case copyright:
                o = "Copyright";
                break;
            case user_id:
                o = "User ID";
                break;
            case cust_id:
                o = "Customer ID";
                break;
            case order_number:
                o = "Order #";
                break;

            default:
                assert (false);
        }
        return o;
    }
}
