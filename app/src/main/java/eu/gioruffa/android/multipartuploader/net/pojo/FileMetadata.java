package eu.gioruffa.android.multipartuploader.net.pojo;
//LISTING_AFTER
public class FileMetadata {
    String owner;
    String[] tags;

    public FileMetadata(String owner, String[] tags) {
        this.owner = owner;
        this.tags = tags;
    }
}
