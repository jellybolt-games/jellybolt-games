const { withAndroidManifest } = require('@expo/config-plugins');

module.exports = function withAndroidRtl(config) {
  return withAndroidManifest(config, (configWithManifest) => {
    const application = configWithManifest.modResults.manifest.application?.[0]?.$;

    if (application) {
      application['android:supportsRtl'] = 'true';
    }

    return configWithManifest;
  });
};
