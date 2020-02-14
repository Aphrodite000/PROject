drop table if exists file_meta;

create table if not exists file_meta(
    name varchar(20) not null,
    path varchar(1000) not null,
    size bigint not null,   --long类型
    last_modified timestamp not null,
    pinyin varchar(50),--拼音和拼音首字母  允许为空 因为不包含中文的时候，就没有拼音和首字母了
    pinyin_first varchar(50)
    --有一个方法判断文件名是否为中文
)