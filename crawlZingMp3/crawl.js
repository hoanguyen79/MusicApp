const fs = require('fs');
const ZingMp3 = require('./lib/ZingMp3');
const {MIGRATION_PATH} = require('./config');

const aliasArtists = new Set();

async function getTop100() {
    const zingChart = await ZingMp3.getDetailPlaylist("ZO68OC68");
    const songs = new Set();

    for (const promise of zingChart.song.items.map(song => ZingMp3.getFullInfo(song.encodeId))) {
        let song = await promise;

        let aliases = song.artists.map(artist => artist.link.split("/").reverse()[0]);

        aliasArtists.add(...aliases);

        songs.add({
            name: song.title,
            artists: song.artists.map(artist => artist.name),
            thumbnail: song.thumbnail.replace('w94', 'w450'),
            listens: song.listen,
            like: song.like,
            year: new Date(song.releaseDate * 1000).getFullYear(),
            mp3: song.streaming['128'],
            albums: [],
            duration: song.duration
        });

        console.log("Đã lấy được: " + songs.size + " bài hát");
    }

    const json = JSON.stringify([...songs], null, 4);

    if (!fs.existsSync(MIGRATION_PATH)) {
        fs.mkdirSync(MIGRATION_PATH);
    }

    fs.writeFileSync(`${MIGRATION_PATH}/songs.json`, json);
}

async function crawlArtistInfo() {
    const artists = new Set();

    for (const alias of aliasArtists) {
        const info = await ZingMp3.getInfoArtist(alias);

        artists.add({
            "biography": info.biography,
            "birthday": info.birthday,
            "cover": info.cover,
            "follow": info.follow,
            "name": info.name,
            "national": info.national,
            "realname": info.realname,
            "sortBiography": info.sortBiography,
            "thumbnail": info.thumbnail.replace("w240", "w450"),
        });

        console.log("Đã lấy được: " + artists.size + " ca sĩ");
    }

    const json = JSON.stringify([...artists], null, 4);

    if (!fs.existsSync(MIGRATION_PATH)) {
        fs.mkdirSync(MIGRATION_PATH);
    }

    fs.writeFileSync(`${MIGRATION_PATH}/artists.json`, json);
}

(async () => {
    await getTop100();
    await crawlArtistInfo();
})();

