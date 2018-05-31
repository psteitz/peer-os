package io.subutai.common.protocol;


//TODO rename to Template after migration complete
public class Templat
{
    private String id;
    private String md5;
    private long size;
    private String name;
    private String version;
    private String owner;
    private String parent;


    public Templat( final String id, final String md5, final long size, final String name, final String version,
                    final String parent )
    {
        this.id = id;
        this.md5 = md5;
        this.size = size;
        this.name = name;
        this.version = version;
        this.parent = parent;
    }


    @Override
    public String toString()
    {
        return "{" + "id='" + id + '\'' + ", md5='" + md5 + '\'' + ", size=" + size + ", name='" + name + '\''
                + ", version='" + version + '\'' + ", owner='" + owner + '\'' + ", parent='" + parent + '\'' + '}';
    }


    public String getId()
    {
        return id;
    }


    public String getMd5()
    {
        return md5;
    }


    public long getSize()
    {
        return size;
    }


    public String getName()
    {
        return name;
    }


    public String getVersion()
    {
        return version;
    }


    public String getOwner()
    {
        return owner;
    }


    public String getParent()
    {
        return parent;
    }
}
