package org.jaudiotagger.audio.aiff;

import org.jaudiotagger.audio.generic.GenericTag;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

public class AiffTag extends GenericTag {

    public boolean hasField(AiffTagFieldKey fieldKey) {
        return hasField(fieldKey.name());
    }

    /**
     * Create new AIFF-specific field and set it in the tag
     *
     * @param genericKey
     * @param value
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public void setField(AiffTagFieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        TagField tagfield = createField(genericKey, value);
        setField(tagfield);
    }

    public TagField createField(AiffTagFieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        return new AiffTagTextField(genericKey.name(), value);
    }

    private class AiffTagTextField implements TagTextField {

        /**
         * Stores the identifier.
         */
        private final String id;
        /**
         * Stores the string.
         */
        private String content;

        /**
         * Creates an instance.
         *
         * @param fieldId        The identifier.
         * @param initialContent The string.
         */
        public AiffTagTextField(String fieldId, String initialContent) {
            this.id = fieldId;
            this.content = initialContent;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#copyContent(org.jaudiotagger.tag.TagField)
         */
        public void copyContent(TagField field) {
            if (field instanceof TagTextField) {
                this.content = ((TagTextField) field).getContent();
            }
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#getContent()
         */
        public String getContent() {
            return this.content;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#setContent(java.lang.String)
         */
        public void setContent(String s) {
            this.content = s;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#getEncoding()
         */
        public String getEncoding() {
            return "ISO-8859-1";
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#setEncoding(java.lang.String)
         */
        public void setEncoding(String s) {
            /* Not allowed */
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#getRawContent()
         */
        public byte[] getRawContent() {
            return this.content == null ? new byte[]{} : Utils.getDefaultBytes(this.content, getEncoding());
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isBinary()
         */
        public boolean isBinary() {
            return false;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isBinary(boolean)
         */
        public void isBinary(boolean b) {
            /* not supported */
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isCommon()
         */
        public boolean isCommon() {
            return true;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isEmpty()
         */
        public boolean isEmpty() {
            return this.content.equals("");
        }

        /**
         * (overridden)
         *
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return getContent();
        }
    } // End of AiffTagTextField

}
