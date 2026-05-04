package pt.isel;

import static pt.isel.DynamicMapperKt.loadDynamicMapper;

public class ArtistDto2ArtistBaseline implements Mapper<ArtistDto, Artist> {
    final Mapper<StateDto, Country> stateMapper = loadDynamicMapper(StateDto.class, Country.class);
    final Mapper<SongDto, Track> songMapper = loadDynamicMapper(SongDto.class, Track.class);
    @Override
    public Artist mapFrom(ArtistDto src) {
        return new Artist(
                src.getKind(),
                src.getName(),
                stateMapper.mapFrom(src.getCountry()),
                songMapper.mapFromList(src.getSongs())
        );
    }
}
