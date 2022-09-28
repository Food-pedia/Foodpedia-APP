package kr.ac.cnu.computer.foodpedia_app;

public class RecyclerItem {
    String date;
    String time;
    String result;
    int count;

    String getDate() {
        return this.date;
    }

    String getTime() {
        return this.time;
    }

    String getResult() { return this.result;}

    int getCount() { return this.count;}

    RecyclerItem(String date, String time, String result, int count) {
        this.date = date;
        this.time = time;
        this.result = result;
        this.count = count;
    }
}
