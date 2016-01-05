module Gitlab
  def self.config
    Settings
  end

  VERSION = File.read(Rails.root.join("VERSION")).strip
  REVISION = Gitlab::Popen.popen(%W(#{config.git.bin_path} log --pretty=format:%h -n 1)).first.chomp
end
