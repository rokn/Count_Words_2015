# encoding: utf-8

require 'abstract_unit'

require 'fileutils'
require 'open-uri'
require 'tmpdir'

class Downloader
  def self.download(from, to)
    unless File.exist?(to)
      $stderr.puts "Downloading #{from} to #{to}"
      unless File.exist?(File.dirname(to))
        system "mkdir -p #{File.dirname(to)}"
      end
      open(from) do |source|
        File.open(to, 'w') do |target|
          source.each_line do |l|
            target.write l
          end
        end
       end
     end
  end
end

class MultibyteGraphemeBreakConformanceTest < ActiveSupport::TestCase
  TEST_DATA_URL = "http://www.unicode.org/Public/#{ActiveSupport::Multibyte::Unicode::UNICODE_VERSION}/ucd/auxiliary"
  TEST_DATA_FILE = '/GraphemeBreakTest.txt'
  CACHE_DIR = File.join(Dir.tmpdir, 'cache')

  def setup
    FileUtils.mkdir_p(CACHE_DIR)
    Downloader.download(TEST_DATA_URL + TEST_DATA_FILE, CACHE_DIR + TEST_DATA_FILE)
  end

  def test_breaks
    each_line_of_break_tests do |*cols|
      *clusters, comment = *cols
      packed = ActiveSupport::Multibyte::Unicode.pack_graphemes(clusters)
      assert_equal clusters, ActiveSupport::Multibyte::Unicode.unpack_graphemes(packed), comment
    end
  end

  protected
    def each_line_of_break_tests(&block)
      lines = 0
      max_test_lines = 0 # Don't limit below 21, because that's the header of the testfile
      File.open(File.join(CACHE_DIR, TEST_DATA_FILE), 'r') do | f |
        until f.eof? || (max_test_lines > 21 and lines > max_test_lines)
          lines += 1
          line = f.gets.chomp!
          next if (line.empty? || line =~ /^\#/)

          cols, comment = line.split("#")
          # Cluster breaks are represented by ÷
          clusters = cols.split("÷").map{|e| e.strip}.reject{|e| e.empty? }
          clusters = clusters.map do |cluster|
            # Codepoints within each cluster are separated by ×
            codepoints = cluster.split("×").map{|e| e.strip}.reject{|e| e.empty? }
            # codepoints are in hex in the test suite, pack wants them as integers
            codepoints.map{|codepoint| codepoint.to_i(16)}
          end

          # The tests contain a solitary U+D800 <Non Private Use High
          # Surrogate, First> character, which Ruby does not allow to stand
          # alone in a UTF-8 string. So we'll just skip it.
          next if clusters.flatten.include?(0xd800)

          clusters << comment.strip

          yield(*clusters)
        end
      end
    end
end
