require 'spec_helper'

describe Gitlab::Metrics do
  describe '.pool_size' do
    it 'returns a Fixnum' do
      expect(described_class.pool_size).to be_an_instance_of(Fixnum)
    end
  end

  describe '.timeout' do
    it 'returns a Fixnum' do
      expect(described_class.timeout).to be_an_instance_of(Fixnum)
    end
  end

  describe '.enabled?' do
    it 'returns a boolean' do
      expect([true, false].include?(described_class.enabled?)).to eq(true)
    end
  end

  describe '.hostname' do
    it 'returns a String containing the hostname' do
      expect(described_class.hostname).to eq(Socket.gethostname)
    end
  end

  describe '.last_relative_application_frame' do
    it 'returns an Array containing a file path and line number' do
      file, line = described_class.last_relative_application_frame

      expect(line).to eq(__LINE__ - 2)
      expect(file).to eq('spec/lib/gitlab/metrics_spec.rb')
    end
  end

  describe '#submit_metrics' do
    it 'prepares and writes the metrics to InfluxDB' do
      connection = double(:connection)
      pool       = double(:pool)

      expect(pool).to receive(:with).and_yield(connection)
      expect(connection).to receive(:write_points).with(an_instance_of(Array))
      expect(Gitlab::Metrics).to receive(:pool).and_return(pool)

      described_class.submit_metrics([{ 'series' => 'kittens', 'tags' => {} }])
    end
  end

  describe '#prepare_metrics' do
    it 'returns a Hash with the keys as Symbols' do
      metrics = described_class.
        prepare_metrics([{ 'values' => {}, 'tags' => {} }])

      expect(metrics).to eq([{ values: {}, tags: {} }])
    end

    it 'escapes tag values' do
      metrics = described_class.prepare_metrics([
        { 'values' => {}, 'tags' => { 'foo' => 'bar=' } }
      ])

      expect(metrics).to eq([{ values: {}, tags: { 'foo' => 'bar\\=' } }])
    end

    it 'drops empty tags' do
      metrics = described_class.prepare_metrics([
        { 'values' => {}, 'tags' => { 'cats' => '', 'dogs' => nil } }
      ])

      expect(metrics).to eq([{ values: {}, tags: {} }])
    end
  end

  describe '#escape_value' do
    it 'escapes an equals sign' do
      expect(described_class.escape_value('foo=')).to eq('foo\\=')
    end

    it 'casts values to Strings' do
      expect(described_class.escape_value(10)).to eq('10')
    end
  end
end
