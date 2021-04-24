let request = require("request-promise");
const { FileCookieStore } = require('tough-cookie-file-store');
const fs = require('fs');
const { URL_API, API_KEY, SERCRET_KEY, COOKIE_PATH, COOKIE_FILE_NAME } = require('../config');
const encrypt = require('./encrypt');

if (!fs.existsSync(COOKIE_PATH)) fs.mkdirSync(COOKIE_PATH);
if (!fs.existsSync(COOKIE_FILE_NAME)) fs.closeSync(fs.openSync(COOKIE_FILE_NAME, 'w'));

let cookiejar = request.jar(new FileCookieStore(COOKIE_FILE_NAME));

request = request.defaults({
    qs: {
        apiKey: API_KEY
    },
    gzip: true,
    json: true,
    jar: cookiejar
});

class ZingMp3 {
    static getFullInfo(id) {
        return new Promise(async (resolve, reject) => {
            try {
                let data = await Promise.all([this.getInfoMusic(id), this.getStreaming(id)]);
                resolve({ ...data[0], streaming: data[1] });
            } catch (err) {
                reject(err);
            }
        });
    }

    static getSectionPlaylist(id) {
        return this.requestZing({
            path: '/api/v2/playlist/getSectionBottom',
            qs: {
                id
            }
        });

    }

    static getDetailPlaylist(id) {
        return this.requestZing({
            path: '/api/v2/playlist/getDetail',
            qs: {
                id
            }
        });

    }

    static getInfoMusic(id) {
        return this.requestZing({
            path: '/api/v2/song/getInfo',
            qs: {
                id
            }
        });
    }

    static getStreaming(id) {
        return this.requestZing({
            path: '/api/v2/song/getStreaming',
            qs: {
                id
            }
        });
    }

    static getHome(page = 1) {
        return this.requestZing({
            path: '/api/v2/home',
            qs: {
                page
            }
        });
    }

    static getInfoArtist(alias) {
        return this.requestZing2({
            path: '/api/v2/artist/getDetail',
            qs: {
                alias,
                version: "1.0.19"
            }
        });
    }

    static async getCookie() {
        if (!cookiejar._jar.store.idx['zingmp3.vn']) await request.get(URL_API);
    }

    static requestZing({ path, qs }) {
        return new Promise(async (resolve, reject) => {
            try {
                await this.getCookie();
                let param = new URLSearchParams(qs).toString();

                let sig = this.hashParam(path, param);

                const data = await request({
                    uri: URL_API + path,
                    qs: {
                        ...qs,
                        ctime: this.time,
                        sig,
                    },
                });

                if (data.err) reject(data);
                resolve(data.data);
            } catch (error) {
                reject(error);
            }
        });
    }

    static requestZing2({ path, qs }) {
        return new Promise(async (resolve, reject) => {
            try {
                await this.getCookie();
                let param = new URLSearchParams(qs).toString();

                let sig = this.hashParam2(path, param);

                const data = await request({
                    uri: URL_API + path,
                    qs: {
                        ...qs,
                        ctime: this.time,
                        sig,
                    },
                });

                if (data.err) reject(data);
                resolve(data.data);
            } catch (error) {
                reject(error);
            }
        });
    }

    static hashParam(path, param = '') {
        this.time = Math.round((new Date).getTime() / 1e3);
        const hash256 = encrypt.getHash256(`ctime=${this.time}${param}`);
        return encrypt.getHmac512(path + hash256, SERCRET_KEY);
    }

    // Để lấy thông tin ca sĩ thì phải xài hàm hash này
    static hashParam2(path, param = '') {
        this.time = Math.round((new Date).getTime() / 1e3);
        const hash256 = encrypt.getHash256(`ctime=${this.time}version=1.0.19`);
        return encrypt.getHmac512(path + hash256, SERCRET_KEY);
    }
}

module.exports = ZingMp3;