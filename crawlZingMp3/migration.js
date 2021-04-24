const fs = require("fs");
const { deleteCollection, insert } = require('./helpers');
const firebase = require("firebase");
require("firebase/firestore");

const { FIREBASE_CONFIG, MIGRATION_PATH } = require('./config');

firebase.initializeApp(FIREBASE_CONFIG);

const db = firebase.firestore();

const collections = [
    {
        "name": "Album Slider",
        "albums": [
            {
                "name": "Rap Việt Ngày Nay",
                "description": "Chất hết mức với những bản Rap Việt ngày nay cùng Zing MP3",
                "cover": "https://photo-zmp3.zadn.vn/banner/3/d/8/d/3d8dc525338137f8408bec2b2101f6e4.jpg"
            },
            {
                "name": "Tết Này Con Không Về",
                "description": "Thanh Hưng như nói thay tâm sự của những đứa con xa quê chưa thể về nhà mùa Tết",
                "cover": "https://photo-zmp3.zadn.vn/banner/6/3/3/c/633c7960ff66521f5192eb4d859920ea.jpg"
            },
            {
                "name": "Hẹn Yêu",
                "description": "Hẹn Yêu' qua phần thể hiện da diết của Minh Vương M4U và Thương Võ",
                "cover": "https://photo-zmp3.zadn.vn/banner/1/4/f/a/14faef38388a4272ac9d3a116d703a6a.jpg"
            }
        ]
    },
];

let songs = JSON.parse(fs.readFileSync(`${MIGRATION_PATH}/songs.json`));
let artists = JSON.parse(fs.readFileSync(`${MIGRATION_PATH}/artists.json`));

(async () => {
    await deleteCollection(db, "collections");
    console.log("Xóa xong bảng collections");

    await deleteCollection(db, "songs");
    console.log("Xóa xong bảng songs");

    await insert(db, "collections", collections);
    console.info("Đã tạo collection 'collections' thành công");

    await insert(db, "songs", songs);
    console.info("Đã tạo collection 'songs' thành công");

    await insert(db, "artists", artists);
    console.info("Đã tạo collection 'artists' thành công");

    console.log("Hoàn tất");
})();
