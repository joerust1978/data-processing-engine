package com.fifththird.edo.processingcore.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class representing an S3 file with metadata.
 * Note: Lombok annotations are included for documentation but manual methods are used
 * due to compilation issues with the annotation processor in this environment.
 */
@Getter
@Setter
public class S3File {
    
    /**
     * The S3 bucket name.
     */
    private String bucket;
    
    /**
     * The S3 file key (path).
     */
    private String fileKey;
    
    /**
     * Whether the file is gzipped compressed.
     */
    private boolean gzipped;
    
    /**
     * Whether the file is PGP encrypted.
     */
    private boolean pgpEncrypted;

    /**
     * Default constructor.
     */
    public S3File() {
    }

    /**
     * Constructor with all fields.
     */
    public S3File(String bucket, String fileKey, boolean gzipped, boolean pgpEncrypted) {
        this.bucket = bucket;
        this.fileKey = fileKey;
        this.gzipped = gzipped;
        this.pgpEncrypted = pgpEncrypted;
    }

    /**
     * Builder pattern implementation.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Manual getter and setter methods (Lombok annotations above for documentation)
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public boolean isGzipped() {
        return gzipped;
    }

    public void setGzipped(boolean gzipped) {
        this.gzipped = gzipped;
    }

    public boolean isPgpEncrypted() {
        return pgpEncrypted;
    }

    public void setPgpEncrypted(boolean pgpEncrypted) {
        this.pgpEncrypted = pgpEncrypted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        S3File s3File = (S3File) o;

        if (gzipped != s3File.gzipped) return false;
        if (pgpEncrypted != s3File.pgpEncrypted) return false;
        if (bucket != null ? !bucket.equals(s3File.bucket) : s3File.bucket != null) return false;
        return fileKey != null ? fileKey.equals(s3File.fileKey) : s3File.fileKey == null;
    }

    @Override
    public int hashCode() {
        int result = bucket != null ? bucket.hashCode() : 0;
        result = 31 * result + (fileKey != null ? fileKey.hashCode() : 0);
        result = 31 * result + (gzipped ? 1 : 0);
        result = 31 * result + (pgpEncrypted ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "S3File{" +
                "bucket='" + bucket + '\'' +
                ", fileKey='" + fileKey + '\'' +
                ", gzipped=" + gzipped +
                ", pgpEncrypted=" + pgpEncrypted +
                '}';
    }

    /**
     * Builder class for S3File.
     */
    public static class Builder {
        private String bucket;
        private String fileKey;
        private boolean gzipped;
        private boolean pgpEncrypted;

        public Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Builder fileKey(String fileKey) {
            this.fileKey = fileKey;
            return this;
        }

        public Builder gzipped(boolean gzipped) {
            this.gzipped = gzipped;
            return this;
        }

        public Builder pgpEncrypted(boolean pgpEncrypted) {
            this.pgpEncrypted = pgpEncrypted;
            return this;
        }

        public S3File build() {
            return new S3File(bucket, fileKey, gzipped, pgpEncrypted);
        }
    }
} 